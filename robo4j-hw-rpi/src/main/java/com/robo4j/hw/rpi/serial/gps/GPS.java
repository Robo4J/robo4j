/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 * 
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

/**
 * Code to talk to the Adafruit "ultimate GPS" over the serial port.
 * FIXME(Marcus/Dec 5, 2016): Should perhaps be moved to type specific package /
 * MTK3339 FIXME(Marcus/May 25, 2017): This thing cannot be allowed to have its
 * own thread...
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class GPS {
	/**
	 * The position accuracy without any
	 */
	public static final float UNAIDED_POSITION_ACCURACY = 3.0f;

	/**
	 * The default read interval to use when auto updating.
	 */
	private static final int DEFAULT_READ_INTERVAL = 550;

	private static final String POSITION_TAG = "$GPGGA";
	private static final String VELOCITY_TAG = "$GPVTG";

	private final Serial serial;
	private final GPSDataRetriever dataRetriever;
	private final int readInterval;

	private final ScheduledExecutorService internalExecutor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "GPS Internal Executor");
			t.setDaemon(true);
			return t;
		}
	});
	private final List<GPSListener> listeners = new CopyOnWriteArrayList<GPSListener>();

	// This is the scheduled future controlling the auto updates.
	private ScheduledFuture<?> scheduledFuture;

	/**
	 * Creates a new GPS instance. Will use an internal thread to read data, and
	 * the default read interval.
	 * 
	 * @throws IOException
	 */
	public GPS() throws IOException {
		this(DEFAULT_READ_INTERVAL);
	}

	/**
	 * Creates a new GPS instance.
	 * 
	 * @throws IOException
	 */
	public GPS(int readInterval) throws IOException {
		this.readInterval = readInterval;
		serial = SerialFactory.createInstance();
		dataRetriever = new GPSDataRetriever();
		initialize();
	}

	/**
	 * Adds a new listener to listen for GPS data.
	 * 
	 * @param gpsListener
	 *            the new listener to add.
	 * 
	 * @see GPSListener
	 */
	public void addListener(GPSListener gpsListener) {
		listeners.add(gpsListener);
	}

	/**
	 * Removes a previously added listener.
	 * 
	 * @param gpsListener
	 *            the listener to remove.
	 * 
	 * @see GPSListener
	 */
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
				scheduledFuture.cancel(true);
			}			
		}
		try {
			serial.close();
		} catch (IllegalStateException | IOException e) {
			// FIXME(Marcus/May 25, 2017): Decide
			e.printStackTrace();
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
	public void startAutoUpdate() {
		synchronized (internalExecutor) {
			if (scheduledFuture == null) {
				scheduledFuture = internalExecutor.scheduleAtFixedRate(dataRetriever, 0, readInterval, TimeUnit.MILLISECONDS);
			}
			throw new IllegalStateException("Auto update already started!");
		}
	}

	private void initialize() throws IOException {
		// Since RaspberryPi 3 nabbed the /dev/ttyAMA0 for the bluetooth,
		// serial0 should be the new logical name to use for the rx/tx pins.
		// This is supposedly compatible with the older raspberry pis as well.
		serial.open("/dev/serial0", 9600);
	}

	private static boolean hasValidCheckSum(String data) {
		if (!data.startsWith("$")) {
			return false;
		}
		int indexOfStar = data.indexOf('*');
		if (indexOfStar <= 0 || indexOfStar >= data.length()) {
			return false;
		}
		String chk = data.substring(1, indexOfStar);
		String checksumStr = data.substring(indexOfStar + 1);
		int valid = Integer.parseInt(checksumStr.trim(), 16);
		int checksum = 0;
		for (int i = 0; i < chk.length(); i++) {
			checksum = checksum ^ chk.charAt(i);
		}
		return checksum == valid;
	}

	private void notifyListeners(PositionEvent event) {
		for (GPSListener listener : listeners) {
			listener.onEvent(event);
		}
	}

	private void notifyListeners(VelocityEvent event) {
		for (GPSListener listener : listeners) {
			listener.onEvent(event);
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
				Logger.getLogger(GPS.class.getName()).log(Level.WARNING, "Error reading line", e);
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
				if (dataLine.startsWith(POSITION_TAG) && hasValidCheckSum(dataLine)) {
					notifyListeners(new PositionEvent(GPS.this, dataLine));
				} else if (dataLine.startsWith(VELOCITY_TAG) && hasValidCheckSum(dataLine)) {
					notifyListeners(new VelocityEvent(GPS.this, dataLine));
				}
			}
		}

		private String readNext(StringBuilder builder) throws IllegalStateException, IOException {
			builder.append(new String(serial.read(), StandardCharsets.US_ASCII));
			return builder.toString();
		}
	}
}
