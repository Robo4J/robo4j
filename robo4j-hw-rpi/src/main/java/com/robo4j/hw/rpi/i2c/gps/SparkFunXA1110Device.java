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
import java.nio.charset.Charset;

import com.pi4j.io.i2c.I2CBus;
import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;

/**
 * Abstraction to read the Titan X1 as delivered in SparFuns XA1110 break-out
 * board.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SparkFunXA1110Device extends AbstractI2CDevice {
	// I'll assume it's ASCII, didn't find any documentation.
	private static final Charset CHARSET = Charset.forName("ASCII");
	private static final int DEFAULT_I2C_ADDRESS = 0x10;
	private static final int READ_BUFFER_SIZE = 256;

	public SparkFunXA1110Device() throws IOException {
		this(I2CBus.BUS_1, DEFAULT_I2C_ADDRESS);
	}

	public SparkFunXA1110Device(int bus, int address) throws IOException {
		super(bus, address);
		init();
	}

	public String createMtkPacket(int packetType, String dataField) {
		StringBuilder builder = new StringBuilder();
		builder.append("$PMTK");

		if (packetType < 100) {
			builder.append("0");
		}
		if (packetType < 10) {
			builder.append("0");
		}
		builder.append(packetType);

		if (dataField != null && dataField.length() > 0) {
			builder.append(dataField);
		}
		builder.append("*");

		builder.append(calcCRCforMTK(builder.toString())); // Attach CRC

		// Attach ending bytes
		builder.append('\r'); // Carriage return
		builder.append('\n'); // Line feed

		return builder.toString();
	}

	public void sendMtkPacket(String mtkPacket) throws IOException {
		writeBytes(mtkPacket.getBytes(CHARSET));
	}

	private String calcCRCforMTK(String string) {
		int crc = 0;
		byte[] sentence = string.getBytes(CHARSET);

		for (int i = 0; i < sentence.length; i++) {
			crc ^= sentence[i];
		}

		String output = "";
		if (crc < 10) {
			output += "0";
		}
		output += Integer.toHexString(crc);
		return output;
	}

	/**
	 * Reads all the GPS data that could be found into the StringBuilder.
	 * 
	 * @param builder
	 *            the Builder to read data into.
	 * 
	 * @throws IOException
	 *             if there was a communication problem.
	 */
	private void readGpsData(StringBuilder builder) throws IOException {
		byte[] buffer = new byte[READ_BUFFER_SIZE];
		int bytesRead = 0;
		while ((bytesRead = i2cDevice.read(buffer, 0, buffer.length)) > 0) {
			builder.append(new String(buffer, 0, bytesRead, CHARSET));
		}
	}

	private void init() throws IOException {
		System.out.println("Initializing device");
		i2cDevice.write(0);
		
		int read = i2cDevice.read();
		System.out.println("Init read: " + read);
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		SparkFunXA1110Device device = new SparkFunXA1110Device();
		Thread t = new Thread(() -> {
			while (true) {
				StringBuilder builder = new StringBuilder();
				try {
					System.out.println("Reading i2c bus to builder");
					device.readGpsData(builder);
					String gpsData = builder.toString();
					System.out.println("Got data: " + gpsData);
					gpsData = gpsData.replace("$", "$\n");
					System.out.print(gpsData);
					Thread.sleep(500);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.setDaemon(true);
		System.out.println("Starting reading from GPS. Press enter to quit!");
		t.start();
		System.in.read();
	}
}
