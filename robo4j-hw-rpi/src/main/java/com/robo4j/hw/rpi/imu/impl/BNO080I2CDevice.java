/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.hw.rpi.imu.impl;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.robo4j.hw.rpi.imu.bno.ShtpPacketRequest;
import com.robo4j.hw.rpi.imu.bno.ShtpPacketResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.robo4j.hw.rpi.imu.impl.BNO080SPIDevice.calculateNumberOfBytesInPacket;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BNO080I2CDevice extends AbstractBNO080Device {

	private static final int DEFAULT_I2C_ADDRESS = 0x4b;

	private final int bus;
	private final int address;
	private final I2CDevice i2cDevice;

	public BNO080I2CDevice() throws IOException {
		this(I2CBus.BUS_1, DEFAULT_I2C_ADDRESS);
	}

	public BNO080I2CDevice(int bus, int address) throws IOException {
		this.bus = bus;
		this.address = address;
		try {
			this.i2cDevice = I2CFactory.getInstance(bus).getDevice(address);
		} catch (com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException e) {
			throw new IOException("Unsupported bus", e);
		}
	}

	@Override
	public boolean start(ShtpSensorReport sensorReport, int reportDelay) {

		/*
		 * We expect caller to begin their I2C port, with the speed of their choice
		 * external to the library But if they forget, we start the hardware here.
		 */

		/*
		 * Begin by resetting the IMU
		 */
		try {
			softResetWithFlushAndResponse();
			ShtpPacketRequest productIdRequest = getProductIdRequest();
			sendPacket(productIdRequest, "start");

			TimeUnit.SECONDS.sleep(2);
			boolean active = true;
			int counter = 0;
			while (active && counter < 20) {
				ShtpPacketResponse response = receivePacket();
				if (containsResponseCode(response, ShtpDeviceReport.PRODUCT_ID_RESPONSE)) {
					active = false;
				}
				counter++;
				TimeUnit.MILLISECONDS.sleep(200);

			}

			return !active;

		} catch (InterruptedException | IOException e) {
			e.printStackTrace();

		}

		return false;
	}

	/**
	 * end command to reset IC Read all advertisement packets from sensor The sensor
	 * has been seen to reset twice if we attempt too much too quickly. This seems
	 * to work reliably.
	 */
	private boolean softResetWithFlushAndResponse() throws IOException, InterruptedException {
		ShtpPacketRequest request = getSoftResetPacket();
		sendPacket(request, "softReset");

		int counter = 0;
		while (receivePacket().dataAvailable()) {
			counter++;
		}
		System.out.println("softReset FLUSH1=" + counter);
		counter = 0;
		while (receivePacket().dataAvailable()) {
			counter++;
		}
		System.out.println("softReset FLUSH2 =" + counter);
		System.out.println("softResetFLUSH");

		counter = 0;
		boolean active = true;
		while (active && counter < 300) {
			ShtpPacketResponse response = receivePacket();
			ShtpDeviceReport report = ShtpDeviceReport.getById(response.getBodyFirst());
			if (report.equals(ShtpDeviceReport.COMMAND_RESPONSE)) {
				active = false;
			} else {
				counter++;
			}
			TimeUnit.MILLISECONDS.sleep(40);
		}
		System.out.println("softReset FLUSH3 RECEIVED COMMAND =" + counter);

		return true;
	}

	private ShtpPacketResponse receivePacket() throws IOException {
		byte[] headerBytes = new byte[SHTP_HEADER_SIZE];
		int readHeaderSize = i2cDevice.read(headerBytes, 0, SHTP_HEADER_SIZE);
		if (readHeaderSize != SHTP_HEADER_SIZE) {
			return new ShtpPacketResponse(0);
		} else {

			int packetLSB = toInt8U(headerBytes[0]);
			int packetMSB = toInt8U(headerBytes[1]);
			int channelNumber = toInt8U(headerBytes[2]);
			int sequenceNumber = toInt8U(headerBytes[3]);

			// Calculate the number of data bytes in this packet
			int dataLength = calculateNumberOfBytesInPacket(packetMSB, packetLSB);
			dataLength -= SHTP_HEADER_SIZE;

			if (dataLength <= 0) {
				return new ShtpPacketResponse(0);
			}
			ShtpPacketResponse response = new ShtpPacketResponse(dataLength);
			response.addHeader(packetLSB, packetMSB, channelNumber, sequenceNumber);

            System.out.println("response dataLength: " + dataLength);
            System.out.println("response header: " + response);

			byte[] responseBytes = new byte[dataLength];
			int readBodySize = i2cDevice.read(responseBytes, 0, dataLength);

			if (readBodySize == dataLength) {
				for (int i = 0; i < dataLength; i++) {
					response.addBody(i, responseBytes[i] & 0xFF);
				}
			} else {
				System.out.println("receivePacket dataLength=" + dataLength + ", readBodySize= " + readBodySize);
			}
			return response;
		}
	}

	private boolean sendPacket(ShtpPacketRequest packet, String message) throws IOException {

		for (int i = 0; i < packet.getHeaderSize(); i++) {
			i2cDevice.write(packet.getHeaderByte(i));
		}

		for (int i = 0; i < packet.getBodySize(); i++) {
			i2cDevice.write(packet.getBodyByte(i));
		}
		return true;
	}

}
