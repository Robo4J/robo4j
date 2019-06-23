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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

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
