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
import com.robo4j.hw.rpi.serial.ydlidar.HealthInfo.HealthStatus;
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
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class YDLidarDevice {
	public static final String SERIAL_PORT_AUTO = "auto";

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

	private static final int RESPONSE_HEADER_LENGTH = 7;
	private static final int CMDFLAG_HAS_PAYLOAD = 0x80;
	private static final byte CMD_SYNC_BYTE = (byte) 0xA5;
	private static final int BAUD_RATE = 230400;

	private final Serial serial;
	private final String serialPort;

	/**
	 * The commands available.
	 */
	public enum Command {
		//@formatter:off
		FORCE_STOP(0x00),
		LOW_POWER_CONSUMPTION(0x01),
		LOW_POWER_SHUTDOWN(0x02),
		/**
		 * This command is used to enable the constant frequency of the system. 
		 * After being enabled, when the lidar is in scanning mode, it will 
		 * automatically adjust the speed so that the scanning frequency will 
		 * be stabilized at the currently set scanning frequency. 
		 * G4 defaults to constant frequency.
		 */
		CONSTANT_FREQUENCY_ON(0x0E),
		/**
		 * This command is used to shut down the system constant frequency. 
		 * After the radar is turned off, the lidar does not perform automatic 
		 * speed adjustment in the scanning mode. 
		 */
		CONSTANT_FREQUENCY_OFF(0x0F),
		/**
		 * G4 enters a soft reboot and the system restarts. 
		 * This command does not answer.
		 */
		RESTART(0x40),
		/**
		 * Enters scan mode and feeds back point cloud data.
		 */
		SCAN(0x60),
		FORCE_SCAN(0x61),
		STOP(0x65),
		RESET(0x80),
		GET_EAI(0x55),
		GET_DEVICE_INFO(0x90),
		GET_DEVICE_HEALTH(0x92),
		/**
		 * This command is used to set the system's ranging frequency and switch the 
		 * ranging frequency between 4 KHz, 8 KHz and 9 KHz. 
		 * The default ranging frequency is 9 KHz. 
		 */
		SET_RANGING_FREQUENCY(0xD0),
		/**
		 * This command is used to obtain the current ranging frequency of the system. 
		 */
		GET_RANGING_FREQUENCY(0xD1);
		//@formatter:on

		int instructionCode;

		Command(int instructionCode) {
			this.instructionCode = instructionCode;
		}

		public int getInstructionCode() {
			return instructionCode;
		}
	}

	public enum IdleMode {
		NORMAL, LOW_POWER
	}

	public enum RangingFrequency {
		FOUR_KHZ((byte) 0x00, 4), EIGHT_KHZ((byte) 0x01, 8), NINE_KHZ((byte) 0x02, 9);

		byte frequencyCode;
		int frequency;

		RangingFrequency(byte frequencyCode, int frequency) {
			this.frequencyCode = frequencyCode;
			this.frequency = frequency;
		}

		public byte getFrequencyCode() {
			return frequencyCode;
		}

		/**
		 * Returns the RangingFrequency from the ydlidar specific frequency
		 * code.
		 * 
		 * @param frequencyCode
		 *            the ydlidar specific frequency code for which to get the
		 *            {@link RangingFrequency}.
		 * @return the {@link RangingFrequency} or null, if no matching
		 *         {@link RangingFrequency} could be found.∫
		 */
		public static RangingFrequency fromFrequencyCode(int frequencyCode) {
			for (RangingFrequency frequency : values()) {
				if (frequency.getFrequencyCode() == frequencyCode) {
					return frequency;
				}
			}
			return null;
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

	/**
	 * Returns information about the ydlidar, such as the version.
	 * 
	 * @return information about the ydlidar, such as the version.
	 * 
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public DeviceInfo getDeviceInfo()
			throws IllegalStateException, IOException, InterruptedException, TimeoutException {
		synchronized (this) {
			disableDataGrabbing();
			sendCommand(Command.GET_DEVICE_INFO);
			ResponseHeader response = readResponseHeader(800);
			validateResponseType(response, ResponseType.DEVICE_INFO);
			byte[] readData = SerialUtil.readBytes(serial, response.getResponseLength(), 800);
			byte[] serialVersion = new byte[16];
			System.arraycopy(readData, 4, serialVersion, 0, serialVersion.length);
			return new DeviceInfo(readData[0], readData[1] << 8 + readData[2], readData[3], serialVersion);
		}
	}

	/**
	 * Returns health information about the ydlidar.
	 * 
	 * @return health information about the ydlidar.
	 * 
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public HealthInfo getHealthInfo()
			throws IllegalStateException, IOException, InterruptedException, TimeoutException {
		synchronized (this) {
			disableDataGrabbing();
			sendCommand(Command.GET_DEVICE_HEALTH);
			ResponseHeader response = readResponseHeader(800);
			validateResponseType(response, ResponseType.DEVICE_HEALTH);
			byte[] readData = SerialUtil.readBytes(serial, response.getResponseLength(), 800);
			return new HealthInfo(HealthStatus.fromStatusCode(readData[0]), (short) (readData[1] << 8 + readData[2]));
		}
	}

	/**
	 * Returns the currently set {@link RangingFrequency}.
	 * 
	 * @return the currently set {@link RangingFrequency}, or null.
	 * 
	 * @throws IOException
	 *             on communication error.
	 * @throws IllegalStateException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 *             if the {@link RangingFrequency} could not be read in time.
	 */
	public RangingFrequency getRangingFrequency()
			throws IOException, IllegalStateException, InterruptedException, TimeoutException {
		synchronized (this) {
			disableDataGrabbing();
			sendCommand(Command.GET_RANGING_FREQUENCY);
			ResponseHeader response = readResponseHeader(800);
			validateResponseType(response, ResponseType.DEVICE_INFO);
			byte[] readData = SerialUtil.readBytes(serial, response.getResponseLength(), 800);
			return RangingFrequency.fromFrequencyCode(readData[0]);
		}
	}

	public void setRangingFrequency(RangingFrequency rangingFrequency)
			throws IOException, IllegalStateException, InterruptedException, TimeoutException {
		synchronized (this) {
			disableDataGrabbing();
			sendCommand(Command.SET_RANGING_FREQUENCY, new byte[] { rangingFrequency.getFrequencyCode() });
			ResponseHeader response = readResponseHeader(800);
			validateResponseType(response, ResponseType.DEVICE_INFO);
			byte[] readData = SerialUtil.readBytes(serial, response.getResponseLength(), 800);
			if (readData.length != 1 || readData[0] != rangingFrequency.getFrequencyCode()) {
				throw new IOException("Unexpected response " + readData.length);
			}
		}
	}

	/**
	 * Sets the low power mode.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws IllegalStateException
	 * @throws TimeoutException
	 */
	public void setIdleMode(IdleMode mode)
			throws IOException, IllegalStateException, InterruptedException, TimeoutException {
		synchronized (this) {
			disableDataGrabbing();
			if (mode == IdleMode.LOW_POWER) {
				sendCommand(Command.LOW_POWER_CONSUMPTION);
			} else {
				sendCommand(Command.LOW_POWER_SHUTDOWN);
			}
			ResponseHeader response = readResponseHeader(800);
			validateResponseType(response, ResponseType.DEVICE_INFO);
			byte[] readData = SerialUtil.readBytes(serial, response.getResponseLength(), 800);
			int expectedResponse = mode == IdleMode.LOW_POWER ? 0x01 : 0x00;
			if (readData.length != 1 || readData[0] != expectedResponse) {
				throw new IOException("Unexpected response " + readData.length);
			}
		}
	}

	/**
	 * Shuts down the ydlidar and releases resources. After this, no more
	 * communication will be possible.
	 */
	public void shutdown() {
		try {
			stopMotor();
			serial.close();
		} catch (IllegalStateException | IOException | InterruptedException e) {
			Logger.getLogger(YDLidarDevice.class.getName()).log(Level.WARNING, "Problem shutting down ydlidar serial",
					e);
		}
	}

	@Override
	public String toString() {
		return "ydlidar@" + serialPort;
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
		synchronized (this) {
			serial.setDTR(false);
			Thread.sleep(500);
		}
	}

	private void startMotor() throws IllegalStateException, IOException, InterruptedException {
		synchronized (this) {
			serial.setDTR(true);
			Thread.sleep(500);
		}
	}

	private ResponseHeader readResponseHeader(long timeout)
			throws IllegalStateException, IOException, InterruptedException, TimeoutException {
		byte[] readBytes = SerialUtil.readBytes(serial, RESPONSE_HEADER_LENGTH, timeout);
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

	private static void validateResponseType(ResponseHeader header, ResponseType expected) throws IOException {
		if (!header.isValid()) {
			throw new IOException("Got bad response!");
		}
		if (header.getResponseType() != expected) {
			throw new IOException("Got the wrong response type. Should have been " + expected + ". Got "
					+ header.getResponseType() + ".");
		}

	}

	/**
	 * Just trying something out...
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws TimeoutException
	 * @throws IllegalStateException
	 */
	public static void main(String[] args)
			throws IOException, InterruptedException, IllegalStateException, TimeoutException {
		YDLidarDevice device = new YDLidarDevice();
		System.out.println(device);
		System.out.println(device.getDeviceInfo());
		System.out.println(device.getHealthInfo());
		System.out.println(device.getRangingFrequency());
		device.shutdown();
		// Naughty that this has to be done... Perhaps fix Pi4J?
		SerialFactory.shutdown();
	}
}
