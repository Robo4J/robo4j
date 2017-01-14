/*
 * Copyright (C) 2017. Miroslav Wengner, Marcus Hirt
 * This GPS.java  is part of robo4j.
 * module: robo4j-hw-rpi
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.hw.rpi.serial.gps;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

/**
 * Code to talk to the Adafruit "ultimate GPS" over the serial port.
 * FIXME(Marcus/Dec 5, 2016): Should perhaps be moved to type specific package / MTK3339 
 * 
 * @author Marcus Hirt
 */
public class GPS {
	/** 
	 * The position accuracy without any 
	 */
	public static final float UNAIDED_POSITION_ACCURACY = 3.0f;
	private static final String POSITION_TAG = "$GPGGA";
	private static final String VELOCITY_TAG = "$GPVTG";

	private final Serial serial;
	private final GPSDataRetriever dataRetriever = new GPSDataRetriever();
	private final Thread dataRetrieverThread;
	private final List<GPSListener> listeners = new CopyOnWriteArrayList<GPSListener>();

	/**
	 * Creates a new GPS instance.
	 * @throws IOException 
	 */
	public GPS() throws IOException {
		serial = SerialFactory.createInstance();
		dataRetrieverThread = new Thread(dataRetriever, "GPS Data Retriever");
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
		dataRetriever.isRunning = false;
		try {
			dataRetrieverThread.join();
		} catch (InterruptedException e) {
		}
	}

	private void initialize() throws IOException {
		serial.open(Serial.DEFAULT_COM_PORT, 9600);
		dataRetrieverThread.start();
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
		private static final int DEFAULT_READ_INTERVAL = 550;
		volatile boolean isRunning = true;
		StringBuilder builder = new StringBuilder();

		@Override
		public void run() {
			while (isRunning) {
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
				sleep(DEFAULT_READ_INTERVAL);
			}
			try {
				serial.close();
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
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

		private void sleep(int millis) {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
			}
		}

		private String readNext(StringBuilder builder) throws IllegalStateException, IOException {
			int available = serial.available();
			for (int i = 0; i < available; i++) {
				builder.append(serial.read());
			}
			return builder.toString();
		}
	}
}
