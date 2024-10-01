/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.hw.rpi.serial.gps;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.Pi4J;
import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.Serial;
import com.robo4j.hw.rpi.gps.GPS;
import com.robo4j.hw.rpi.gps.GPSListener;
import com.robo4j.hw.rpi.gps.NmeaUtils;

/**
 * Code to talk to the Adafruit "ultimate GPS" over the serial port.
 * FIXME(Marcus/Dec 5, 2016): Should perhaps be moved to type specific package /
 * MTK3339 FIXME(Marcus/May 25, 2017): This thing cannot be allowed to have its
 * own thread...
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class MTK3339GPS implements GPS {
	/**
	 * The position accuracy without any
	 */
	public static final float UNAIDED_POSITION_ACCURACY = 3.0f;

	/**
	 * The default read interval to use when auto updating.
	 */
	public static final int DEFAULT_READ_INTERVAL = 550;

	/**
	 * The default serial port is /dev/serial0. Since Raspberry Pi 3 nabbed the
	 * /dev/ttyAMA0 for the bluetooth, serial0 should be the new logical name to
	 * use for the rx/tx pins. This is supposedly compatible with the older
	 * Raspberry Pis as well.
	 */
	public static final String DEFAULT_GPS_PORT = "/dev/serial0";

	private static final String POSITION_TAG = "$GPGGA";
	private static final String VELOCITY_TAG = "$GPVTG";

	private static final int BAUD_DEFAULT = 9600;

	private final String serialPort;
	private final Serial serial;
//	private final ExecutorServiceFactory serviceFactory;
	private final GPSDataRetriever dataRetriever;
	private final int readInterval;

	private final ScheduledExecutorService internalExecutor = Executors.newScheduledThreadPool(1, (r) -> {
		Thread t = new Thread(r, "GPS Internal Executor");
		t.setDaemon(true);
		return t;

	});
	private final List<GPSListener> listeners = new CopyOnWriteArrayList<GPSListener>();

	// This is the scheduled future controlling the auto updates.
	private ScheduledFuture<?> scheduledFuture;

	/**
	 * Creates a new GPS instance. Will use an internal thread to read data, and
	 * the default read interval.
	 *
	 * @throws IOException
	 *             exception
	 */
	public MTK3339GPS() throws IOException {
		this(DEFAULT_GPS_PORT, DEFAULT_READ_INTERVAL);
	}

	/**
	 * Creates a new GPS instance.
	 *
	 * @param serialPort
	 *            serial port
	 * @param readInterval
	 *            read internal
	 *
	 * @throws IOException
	 *             exception
	 */
	public MTK3339GPS(String serialPort, int readInterval) throws IOException {
		this.serialPort = serialPort;
		this.readInterval = readInterval;
//		serial = SerialFactory.createInstance();
//		serviceFactory = SerialFactory.getExecutorServiceFactory();

		var pi4jRpiContext = Pi4J.newAutoContext();
		this.serial = pi4jRpiContext.create(Serial.newConfigBuilder(pi4jRpiContext)
				.id(serialPort)
				//TODO review default baud
				.baud(Baud._9600).build());

		dataRetriever = new GPSDataRetriever();
		initialize();
	}

	@Override
	public void addListener(GPSListener gpsListener) {
		listeners.add(gpsListener);
	}

	@Override
	public void removeListener(GPSListener gpsListener) {
		listeners.remove(gpsListener);
	}

	/**
	 * Shuts down the GPS listener. After the shutdown is completed, no more
	 * events will be sent to listeners.
	 */
	public void shutdown() {
		synchronized (internalExecutor) {
			if (scheduledFuture != null) {
				scheduledFuture.cancel(false);
			}
			awaitTermination();
		}
		try {
			serial.close();
			// // FIXME(Marcus/Jul 30, 2017): This is kinda scary to do in a
			// component!
			// TODO review
//			serviceFactory.shutdown();
		} catch (IllegalStateException e) {
			// Don't care, we're shutting down.
		}
	}

	private void awaitTermination() {
		try {
			internalExecutor.awaitTermination(10, TimeUnit.MILLISECONDS);
			internalExecutor.shutdown();
		} catch (InterruptedException e) {
			// Don't care if we were interrupted.
		}
	}

	/**
	 * Call this to perform a read of data from the device in the current
	 * thread. Use this for scheduling reads yourself.
	 */
	public void update() {
		dataRetriever.update();
	}

	/**
	 * Call this to start reading automatically, using an internal scheduler.
	 */
	public void start() {
		synchronized (internalExecutor) {
			if (scheduledFuture == null) {
				scheduledFuture = internalExecutor.scheduleAtFixedRate(dataRetriever, 0, readInterval, TimeUnit.MILLISECONDS);
			}
			throw new IllegalStateException("Auto update already started!");
		}
	}

	private void initialize() throws IOException {
		serial.open();
//		serial.open(serialPort, BAUD_DEFAULT);
	}

	private void notifyListeners(MTK3339PositionEvent event) {
		for (GPSListener listener : listeners) {
			listener.onPosition(event);
		}
	}

	private void notifyListeners(MTK3339VelocityEvent event) {
		for (GPSListener listener : listeners) {
			listener.onVelocity(event);
		}
	}

	private final class GPSDataRetriever implements Runnable {
		private final StringBuilder builder = new StringBuilder();

		@Override
		public void run() {
			update();
		}

		public void update() {
			String str = "";
			try {
				str = readNext(builder);
			} catch (IllegalStateException | IOException e) {
				Logger.getLogger(MTK3339GPS.class.getName()).log(Level.WARNING, "Error reading line", e);
			}
			builder.setLength(0);
			StringTokenizer st = new StringTokenizer(str, "\n", true);
			while (st.hasMoreElements()) {
				String dataLine = st.nextToken();
				while ("\n".equals(dataLine) && st.hasMoreElements()) {
					dataLine = st.nextToken();
				}
				if (st.hasMoreElements()) {
					consume(dataLine);
				} else if (!"\n".equals(dataLine)) {
					builder.append(dataLine);
				}
			}
		}

		private void consume(String dataLine) {
			if (dataLine.startsWith("$")) {
				if (dataLine.startsWith(POSITION_TAG) && NmeaUtils.hasValidCheckSum(dataLine)) {
					notifyListeners(new MTK3339PositionEvent(MTK3339GPS.this, dataLine));
				} else if (dataLine.startsWith(VELOCITY_TAG) && NmeaUtils.hasValidCheckSum(dataLine)) {
					notifyListeners(new MTK3339VelocityEvent(MTK3339GPS.this, dataLine));
				}
			}
		}

		private String readNext(StringBuilder builder) throws IllegalStateException, IOException {
//			builder.append(new String(serial.read(), StandardCharsets.US_ASCII));
			// TODO : improve
			var buffer = new byte[serial.available()];
			serial.read(buffer);
			builder.append(new String(buffer, StandardCharsets.US_ASCII));
			return builder.toString();
		}
	}
}
