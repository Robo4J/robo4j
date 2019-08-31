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
package com.robo4j.hw.rpi.serial;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import com.pi4j.io.serial.Serial;
import com.pi4j.util.ExecUtil;

public class SerialUtil {

	public static Set<SerialDeviceDescriptor> getAvailableUSBSerialDevices() throws IOException, InterruptedException {
		Set<SerialDeviceDescriptor> descriptors = new HashSet<>();
		String[] devices = ExecUtil.execute("ls /sys/bus/usb-serial/devices/");
		for (String device : devices) {
			Map<String, String> metadata = asMetadata(ExecUtil.execute("cat /sys/bus/usb-serial/devices/" + device + "/../uevent"));
			String path = "/dev/" + device;
			descriptors.add(createSerialDescriptor(path, metadata));
		}
		return descriptors;
	}

	/**
	 * Will attempt to read to read the indicated number of bytes from the
	 * serial port.
	 * 
	 * @param serial
	 *            the (opened) serial port to read from.
	 * @param timeout
	 *            the timeout after which to fail.
	 * @return the byte [] with the result.
	 * @throws IOException
	 * @throws IllegalStateException
	 * @throws InterruptedException
	 */
	public static byte[] readBytes(Serial serial, int bytes, long timeout)
			throws IllegalStateException, IOException, InterruptedException, TimeoutException {
		ByteBuffer buffer = ByteBuffer.allocate(bytes);
		readBytes(buffer, serial, bytes, timeout);
		return buffer.array();
	}

	/**
	 * Will attempt to read to read the indicated number of bytes from the
	 * serial port into the provided buffer. It's up to you to ensure that there
	 * is enough space to hold the data.
	 * 
	 * @param buffer
	 *            the byte buffer to read into.
	 * @param serial
	 *            the (opened) serial port to read from.
	 * @param bytes
	 *            the number of bytes to read.
	 * @param timeout
	 *            the timeout after which to fail.
	 * @throws IOException
	 * @throws IllegalStateException
	 * @throws InterruptedException
	 */
	public static void readBytes(ByteBuffer buffer, Serial serial, int bytes, long timeout)
			throws IllegalStateException, IOException, InterruptedException, TimeoutException {
		int available = serial.available();

		if (available >= bytes) {
			serial.read(bytes, buffer);
			return;
		}

		long startTime = System.currentTimeMillis();
		int leftToRead = bytes;
		while (leftToRead > 0) {
			if (System.currentTimeMillis() - startTime > timeout) {
				throw new TimeoutException("Could not read bytes in time");
			}
			available = serial.available();
			if (available == 0) {
				Thread.sleep(40);
				continue;
			}
			int nextRead = Math.min(leftToRead, available);
			serial.read(nextRead, buffer);
			leftToRead -= nextRead;
		}
	}

	private static Map<String, String> asMetadata(String[] lines) {
		Map<String, String> metadata = new HashMap<>();
		for (String line : lines) {
			String[] lineData = line.split("=");
			if (lineData.length == 2) {
				metadata.put(lineData[0], lineData[1]);
			} else {
				Logger.getLogger(SerialUtil.class.getName()).warning("Failed to parse usb serial metadata: " + line);
			}
		}
		return metadata;
	}

	private static SerialDeviceDescriptor createSerialDescriptor(String path, Map<String, String> metadata) {
		String productString = metadata.get("PRODUCT");
		if (productString != null) {
			String[] productStrings = productString.split("/");
			if (productStrings.length >= 2) {
				return new SerialDeviceDescriptor(path, productStrings[0], productStrings[1]);
			}
		}
		return new SerialDeviceDescriptor(path, null, null);
	}

	/**
	 * Prints all the available USB serial devices found.
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		Set<SerialDeviceDescriptor> descriptors = getAvailableUSBSerialDevices();
		if (descriptors.size() == 0) {
			System.out.println("No usb serial devices found!");
		} else {
			System.out.println("Printing usb serial devices found:");
			for (SerialDeviceDescriptor descriptor : descriptors) {
				System.out.println(descriptor);
			}
			System.out.println("Done!");
		}
	}
}
