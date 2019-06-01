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
package com.robo4j.hw.rpi.i2c.gps;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.robo4j.hw.rpi.gps.GPS;
import com.robo4j.hw.rpi.gps.GPSListener;
import com.robo4j.hw.rpi.gps.PositionEvent;
import com.robo4j.hw.rpi.gps.VelocityEvent;

/**
 * The GPS class for the SparkFun GPS.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class TitanX1GPS implements GPS {
	private static final long READ_INTERVAL = 1000;

	private final XA1110Device device;
	private final List<GPSListener> listeners = new CopyOnWriteArrayList<GPSListener>();
	private final GPSDataRetriever retriever = new GPSDataRetriever();

	private final ScheduledExecutorService internalExecutor = Executors.newScheduledThreadPool(1, (r) -> {
		Thread t = new Thread(r, "GPS Internal Executor");
		t.setDaemon(true);
		return t;

	});

	// This is the scheduled future controlling the auto updates.
	private ScheduledFuture<?> scheduledFuture;

	public TitanX1GPS() throws IOException {
		this.device = new XA1110Device();
	}

	public TitanX1GPS(int bus, int address) throws IOException {
		this.device = new XA1110Device(bus, address);
	}

	@Override
	public void addListener(GPSListener gpsListener) {
		listeners.add(gpsListener);
	}

	@Override
	public void removeListener(GPSListener gpsListener) {
		listeners.remove(gpsListener);
	}

	@Override
	public void start() {
		synchronized (internalExecutor) {
			if (scheduledFuture == null) {
				scheduledFuture = internalExecutor.scheduleAtFixedRate(retriever, 0, READ_INTERVAL, TimeUnit.MILLISECONDS);
			} else {
				throw new IllegalStateException("Already running!");
			}
		}
	}

	@Override
	public void shutdown() {
		synchronized (internalExecutor) {
			if (scheduledFuture != null) {
				scheduledFuture.cancel(false);
			}
			awaitTermination();
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
				Logger.getLogger(TitanX1GPS.class.getName()).log(Level.WARNING, "Error reading line", e);
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
				if (hasValidCheckSum(dataLine)) {
					if (XA1110PositionEvent.isAcceptedLine(dataLine)) {
						notifyPosition(XA1110PositionEvent.decode(TitanX1GPS.this, dataLine));
					} else if (XA1110VelocityEvent.isAcceptedLine(dataLine)) {
						notifyVelocity(XA1110VelocityEvent.decode(TitanX1GPS.this, dataLine));
					}
				}
			}
		}

		private String readNext(StringBuilder builder) throws IllegalStateException, IOException {
			device.readGpsData(builder);
			return builder.toString();
		}
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

	private void notifyPosition(PositionEvent event) {
		for (GPSListener listener : listeners) {
			listener.onPosition(event);
		}
	}

	private void notifyVelocity(VelocityEvent event) {
		for (GPSListener listener : listeners) {
			listener.onVelocity(event);
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
}
