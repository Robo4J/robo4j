/*
 * Copyright (c) 2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.hw.rpi.serial.ydlidar;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import com.robo4j.hw.rpi.serial.SerialDeviceDescriptor;
import com.robo4j.hw.rpi.serial.SerialUtil;
import com.robo4j.hw.rpi.serial.ydlidar.ResponseHeader.ResponseType;

/**
 * Driver for the ydlidar device.
 * 
 * <p>
 * To play with it directly from the command line, and ensure that it is happy,
 * you can first set up the device to use the correct settings, e.g. (if on
 * /dev/ttyUSB0):
 * <p>
 * stty -F /dev/ttyUSB0 230400 cs8 -cstopb -parenb
 * <p>
 * Next you can cat the device to list what it is saying, for example cat to
 * file:
 * <p>
 * cat /dev/ttyUSB0 > mytest
 * <p>
 * Of course, to make it say something, you will need to send it a command, for
 * example:
 * <p>
 * echo -en "\xA5\x90" > /dev/ttyUSB0
 * 
 * @author Marcus
 */
public class YDLidarDevice {
	public static String SERIAL_PORT_AUTO = "auto";

	/**
	 * Vendor ID for the CP2102 USB to UART Bridge Controller that is included
	 * with the YDLidar device. Used for auto detection.
	 */
	private static final String VENDOR_ID = "10c4";

	/**
	 * The product ID for the CP2102 USB to UART Bridge Controller that is
	 * included with the YDLidar device. Used for auto detection.
	 */
	private static final String PRODUCT_ID = "ea60";

	private static final int CMDFLAG_HAS_PAYLOAD = 0x80;
	private static final byte CMD_SYNC_BYTE = (byte) 0xA5;
	private static final int BAUD_RATE = 230400;

	private final Serial serial;
	private final String serialPort;

	/**
	 * The commands available.
	 */
	public enum Command {
		STOP(0x65), FORCE_STOP(0x00), SCAN(0x60), FORCE_SCAN(0x61), RESET(0x80), GET_EAI(0x55), GET_DEVICE_INFO(0x90), GET_DEVICE_HEALTH(
				0x92);

		int instructionCode;

		Command(int instructionCode) {
			this.instructionCode = instructionCode;
		}

		public int getInstructionCode() {
			return instructionCode;
		}
	}

	/**
	 * Default constructor. Will attempt to auto detect the USB to UART bridge
	 * controller included with the YDLidar device.
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public YDLidarDevice() throws IOException, InterruptedException {
		this(SERIAL_PORT_AUTO);
	}

	/**
	 * 
	 * @param serialPort
	 *            the serial port to use, or SERIAL_PORT_AUTO if an attempt to
	 *            auto resolve should be made.
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public YDLidarDevice(String serialPort) throws IOException, InterruptedException {
		if (SERIAL_PORT_AUTO.equals(serialPort)) {
			this.serialPort = autoResolveSerialPort();
		} else {
			this.serialPort = serialPort;
		}
		serial = SerialFactory.createInstance();
		serial.open(this.serialPort, BAUD_RATE);
	}

	public DeviceInfo getDeviceInfo() throws IllegalStateException, IOException, InterruptedException, TimeoutException {
		synchronized (this) {
			disableDataGrabbing();
			sendCommand(Command.GET_DEVICE_INFO);
			ResponseHeader response = readResponseHeader(800);
			if (!response.isValid()) {
				throw new IOException("Got bad response!");
			}
			if (response.getResponseType() != ResponseType.DEVICE_INFO) {
				throw new IOException("Got the wrong response type. Should have been " + ResponseType.DEVICE_INFO + ". Got "
						+ response.getResponseType() + ".");
			}
			byte[] readData = SerialUtil.readBytes(serial, 20, 800);
			byte[] serialVersion = new byte[16];
			System.arraycopy(readData, 4, serialVersion, 0, serialVersion.length);
			return new DeviceInfo(readData[0], readData[1] << 8 + readData[2], readData[3], serialVersion);
		}
	}

	public void shutdown() {
		try {
			serial.close();
		} catch (IllegalStateException | IOException e) {
			Logger.getLogger(YDLidarDevice.class.getName()).log(Level.WARNING, "Problem shutting down ydlidar serial", e);
		}
	}

	private void sendCommand(Command command) throws IllegalStateException, IOException {
		sendCommand(command, null);
	}

	private void disableDataGrabbing() throws IllegalStateException, IOException, InterruptedException {
		synchronized (this) {
			sendCommand(Command.STOP);
			stopMotor();
		}
	}

	private void stopMotor() throws IllegalStateException, IOException, InterruptedException {
		serial.setDTR(false);
		Thread.sleep(500);
	}

	private ResponseHeader readResponseHeader(long timeout)
			throws IllegalStateException, IOException, InterruptedException, TimeoutException {
		byte[] readBytes = SerialUtil.readBytes(serial, 7, timeout);
		return new ResponseHeader(readBytes);
	}

	private void sendCommand(Command command, byte[] payload) throws IllegalStateException, IOException {
		int commandByte = command.getInstructionCode();
		if (payload != null) {
			commandByte |= CMDFLAG_HAS_PAYLOAD;
		}

		byte[] header = new byte[2];
		header[0] = CMD_SYNC_BYTE;
		header[1] = (byte) commandByte;
		serial.write(header);
		if (payload != null) {
			int checksum = 0;
			checksum ^= CMD_SYNC_BYTE;
			checksum ^= commandByte;
			checksum ^= (payload.length & 0xFF);
			serial.write((byte) payload.length);
			serial.write(payload);
			serial.write((byte) checksum);
		}
	}

	/*
	 * If this fails, throw an exception. The user should specify the serial
	 * port to use manually instead.
	 */
	private static String autoResolveSerialPort() throws IOException, InterruptedException {
		Set<SerialDeviceDescriptor> availableUSBSerialDevices = SerialUtil.getAvailableUSBSerialDevices();
		for (SerialDeviceDescriptor descriptor : availableUSBSerialDevices) {
			if (VENDOR_ID.equals(descriptor.getVendorId()) && PRODUCT_ID.equals(descriptor.getProductId())) {
				Logger.getLogger(YDLidarDevice.class.getClass().getName()).info("Bound ydlidar to " + descriptor);
				return descriptor.getPath();
			}
		}
		throw new IOException("Failed to auto resolve the serial port used by the ydlidar.");
	}

	public String toString() {
		return "ydlidar@" + serialPort;
	}

	/**
	 * Just trying something out...
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws TimeoutException
	 * @throws IllegalStateException
	 */
	public static void main(String[] args) throws IOException, InterruptedException, IllegalStateException, TimeoutException {
		YDLidarDevice device = new YDLidarDevice();
		System.out.println(device);
		System.out.println(device.getDeviceInfo());
		device.shutdown();
	}
}
