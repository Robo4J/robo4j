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
package com.robo4j.hw.rpi.serial;

import com.pi4j.io.serial.Serial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
//import com.pi4j.util.ExecUtil;

public class SerialUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerialUtil.class);

    // TODO, alternative to ExecUtil
    public static Set<SerialDeviceDescriptor> getAvailableUSBSerialDevices() throws IOException, InterruptedException {
        Set<SerialDeviceDescriptor> descriptors = new HashSet<>();
//		String[] devices = ExecUtil.execute("ls /sys/bus/usb-serial/devices/");
//		for (String device : devices) {
//			Map<String, String> metadata = asMetadata(ExecUtil.execute("cat /sys/bus/usb-serial/devices/" + device + "/../uevent"));
//			String path = "/dev/" + device;
//			descriptors.add(createSerialDescriptor(path, metadata));
//		}
        return descriptors;
    }

    /**
     * Will attempt to read to read the indicated number of bytes from the
     * serial port.
     *
     * @param serial  the (opened) serial port to read from.
     * @param timeout the timeout after which to fail.
     * @return the byte [] with the result.
     * @throws IllegalStateException illegal exception
     * @throws InterruptedException  interrupted exception
     */
    public static byte[] readBytes(Serial serial, int bytes, long timeout)
            throws IllegalStateException, InterruptedException, TimeoutException {
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        readBytes(buffer, serial, bytes, timeout);
        return buffer.array();
    }

    /**
     * Will attempt to read to read the indicated number of bytes from the
     * serial port into the provided buffer. It's up to you to ensure that there
     * is enough space to hold the data.
     *
     * @param buffer  the byte buffer to read into.
     * @param serial  the (opened) serial port to read from.
     * @param bytes   the number of bytes to read.
     * @param timeout the timeout after which to fail.
     * @throws InterruptedException
     */
    public static void readBytes(ByteBuffer buffer, Serial serial, int bytes, long timeout)
            throws IllegalStateException, InterruptedException, TimeoutException {
        int available = serial.available();

        if (available >= bytes) {
            serial.read(buffer, bytes);
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
            serial.read(buffer, nextRead);
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
                LOGGER.warn("Failed to parse usb serial metadata: {}", line);
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
     * @throws InterruptedException interrupted exception
     * @throws IOException          io exception
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        Set<SerialDeviceDescriptor> descriptors = getAvailableUSBSerialDevices();
        if (descriptors.isEmpty()) {
            LOGGER.info("No usb serial devices found!");
        } else {
            LOGGER.info("Printing usb serial devices found:");
            for (SerialDeviceDescriptor descriptor : descriptors) {
                LOGGER.info("descriptor:{}", descriptor);
            }
            LOGGER.info("Done!");
        }
    }
}
