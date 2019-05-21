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

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiMode;
import com.pi4j.io.spi.impl.SpiDeviceImpl;

import java.io.IOException;

/**
 * Abstraction for a BNO080 absolute orientation device.
 *
 * Channel 0: the SHTP command channel Channel 1: executable Channel 2: sensor
 * hub control channel Channel 3: input sensor reports (non-wake, not gyroRV)
 * Channel 4: wake input sensor reports (for sensors configured as wake up
 * sensors) Channel 5: gyro rotation vector
 *
 *
 * https://github.com/sparkfun/SparkFun_BNO080_Arduino_Library/blob/master/src/SparkFun_BNO080_Arduino_Library.cpp
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BNO080SPIDevice {

	public static final SpiMode DEFAULT_SPI_MODE = SpiMode.MODE_0;
	public static final int DEFAULT_SPI_SPEED = 3000000; // 3MHz maximum SPI speed
	public static final short CHANNEL_COUNT = 6; // BNO080 supports 6 channels

	// Registers
	private static final int CHANNEL_COMMAND = 0;
	private static final int CHANNEL_EXECUTABLE = 1;
	private static final int CHANNEL_CONTROL = 2;
	private static final int CHANNEL_REPORTS = 3;
	private static final int CHANNEL_WAKE_REPORTS = 4;
	private static final int CHANNEL_GYRO = 5;

	// All the ways we can configure or talk to the BNO080, figure 34, page 36
	// reference manual
	// These are used for low level communication with the sensor, on channel 2
	private static final int SHTP_REPORT_COMMAND_RESPONSE = 0xF1;
	private static final int SHTP_REPORT_COMMAND_REQUEST = 0xF2;
	private static final int SHTP_REPORT_FRS_READ_RESPONSE = 0xF3;
	private static final int SHTP_REPORT_FRS_READ_REQUEST = 0xF4;
	private static final int SHTP_REPORT_PRODUCT_ID_RESPONSE = 0xF8;
	private static final int SHTP_REPORT_PRODUCT_ID_REQUEST = 0xF9;
	private static final int SHTP_REPORT_BASE_TIMESTAMP = 0xFB;
	private static final int SHTP_REPORT_SET_FEATURE_COMMAND = 0xFD;

	// All the different sensors and features we can get reports from
	// These are used when enabling a given sensor
	private static final int SENSOR_REPORTID_ACCELEROMETER = 0x01;
	private static final int SENSOR_REPORTID_GYROSCOPE = 0x02;
	private static final int SENSOR_REPORTID_MAGNETIC_FIELD = 0x03;
	private static final int SENSOR_REPORTID_LINEAR_ACCELERATION = 0x04;
	private static final int SENSOR_REPORTID_ROTATION_VECTOR = 0x05;
	private static final int SENSOR_REPORTID_GRAVITY = 0x06;
	private static final int SENSOR_REPORTID_GAME_ROTATION_VECTOR = 0x08;
	private static final int SENSOR_REPORTID_GEOMAGNETIC_ROTATION_VECTOR = 0x09;
	private static final int SENSOR_REPORTID_TAP_DETECTOR = 0x10;
	private static final int SENSOR_REPORTID_STEP_COUNTER = 0x11;
	private static final int SENSOR_REPORTID_STABILITY_CLASSIFIER = 0x13;
	private static final int SENSOR_REPORTID_PERSONAL_ACTIVITY_CLASSIFIER = 0x1E;

	// Record IDs from figure 29, page 29 reference manual
	// These are used to begin the metadata for each sensor type
	private static final int FRS_RECORDID_ACCELEROMETER = 0xE302;
	private static final int FRS_RECORDID_GYROSCOPE_CALIBRATED = 0xE306;
	private static final int FRS_RECORDID_MAGNETIC_FIELD_CALIBRATED = 0xE309;
	private static final int FRS_RECORDID_ROTATION_VECTOR = 0xE30B;

	// Command IDs from section 6.4, page 42
	// These are used to calibrate, initialize, set orientation, tare etc the sensor
	private static final int COMMAND_ERRORS = 1;
	private static final int COMMAND_COUNTER = 2;
	private static final int COMMAND_TARE = 3;
	private static final int COMMAND_INITIALIZE = 4;
	private static final int COMMAND_DCD = 6;
	private static final int COMMAND_ME_CALIBRATE = 7;
	private static final int COMMAND_DCD_PERIOD_SAVE = 9;
	private static final int COMMAND_OSCILLATOR = 10;
	private static final int COMMAND_CLEAR_DCD = 11;

	private static final int CALIBRATE_ACCEL = 0;
	private static final int CALIBRATE_GYRO = 1;
	private static final int CALIBRATE_MAG = 2;
	private static final int CALIBRATE_PLANAR_ACCEL = 3;
	private static final int CALIBRATE_ACCEL_GYRO_MAG = 4;
	private static final int CALIBRATE_STOP = 5;

	private static final int MAX_PACKET_SIZE = 128; // Packets can be up to 32k but we don't have that much RAM.
	private static final int MAX_METADATA_SIZE = 9; // This is in words. There can be many but we mostly only care about
													// the first 9 (Qs, range, etc)
	private static final int SHTP_HEADER_SIZE = 4;

	private SpiDevice spiDevice;
	private int imuCSPin = 10;
	private int imuWAKPin = 9;
	private int imuINTPin = 8;
	private int imuRSTPin = 7;
	private GpioController gpio;
	private GpioPinDigitalInput intGpio;
	private GpioPinDigitalOutput wakeGpio;
	private GpioPinDigitalOutput rstGpio;
	private GpioPinDigitalOutput csGpio;
	private int[] shtpData = new int[MAX_PACKET_SIZE];
	private int[] sequenceNumber = new int[CHANNEL_COUNT];
	private int[] shtpHeader = new int[SHTP_HEADER_SIZE]; // Each packet has a header of 4 bytes

	public BNO080SPIDevice() throws IOException, InterruptedException {

	}

	private void init() throws IOException, InterruptedException {
		System.out.println("INIT");
		spiDevice = new SpiDeviceImpl(SpiChannel.CS0, DEFAULT_SPI_SPEED, SpiMode.MODE_3);
		gpio = GpioFactory.getInstance();


		csGpio = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_26, "CS", PinState.HIGH); // Deselect BNO080

		// Configure the BNO080 for SPI communication
		wakeGpio = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_19, "WAK", PinState.HIGH); // Before boot up the PS0/WAK
																							// pin must be high to enter
																							// SPI mode
		rstGpio = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_13, "RST", PinState.LOW); // Reset BNO080
		intGpio = gpio.provisionDigitalInputPin(RaspiPin.GPIO_06, "INT", PinPullResistance.PULL_UP);


		Thread.sleep(2); // Min length not specified in datasheet?
		rstGpio.setState(PinState.HIGH); // Bring out of reset
		System.out.println("INIT DONE 2ms sleep");
	}

	/**
	 * Blocking wait for BNO080 to assert (pull low) the INT pin indicating it's
	 * ready for comm. Can take more than 104ms after a hardware reset
	 */
	private boolean waitForSPI() throws IOException, InterruptedException {
		for (int i = 0; i < 125; i++) {

			if (spiDevice.write((byte)0)[0] == PinState.LOW.getValue() ) {
				return true;
			} else {
				System.out.println("SPI Wait");
			}
			Thread.sleep(1);
		}
		System.out.println("SPI INIT timeout");
		return false;
	}

	/**
	 * Send command to reset IC Read all advertisement packets from sensor The
	 * sensor has been seen to reset twice if we attempt too much too quickly. This
	 * seems to work reliably.
	 */
	private void softReset() throws InterruptedException, IOException {
		shtpData[0] = 1; // Reset

		// Attempt to start communication with sensor
		sendPacket(CHANNEL_EXECUTABLE, 1);
		Thread.sleep(50);

		while (receivePacket())
			;
		Thread.sleep(50);
		while (receivePacket())
			;

	}

	private boolean receivePacket() throws IOException, InterruptedException {
		// we dont have INT
		// if (digitalRead(_int) == HIGH)

		// Old way
		if(spiDevice.write((byte)0)[0] == PinState.HIGH.getValue()){
			System.out.println("receive packet data are not available");
			return false;
		}

		// Get first four bytes to find out how much data we need to read
		csGpio.setState(PinState.LOW);

		// Get the first four bytes, aka the packet header
		int packetLSB = spiDevice.write((byte) 0)[0];
		int packetMSB = spiDevice.write((byte) 0)[0];
		int channelNumber = spiDevice.write((byte) 0)[0];
		int sequenceNumber = spiDevice.write((byte) 0)[0]; // Not sure if we need to store this or not

		shtpHeader[0] = packetLSB;
		shtpHeader[1] = packetMSB;
		shtpHeader[2] = channelNumber;
		shtpHeader[3] = sequenceNumber;

//		for(int i=0; i< SHTP_HEADER_SIZE;i++){
//			System.out.println("HEADER"+i+":"+ Integer.toOctalString(shtpHeader[i]));
//		}
//

		// Calculate the number of data bytes in this packet
		int dataLength = (packetMSB << 8 | packetLSB) & 0xFFFF;
		dataLength &= ~(1 << 15) & 0xFF; // Clear the MSbit.
		// This bit indicates if this package is a continuation of the last. Ignore it
		// for now.
		if (dataLength == 0) {
			System.out.println("receivePacket is EMPTY");
			return false;
		}
		dataLength -= SHTP_HEADER_SIZE;

		// Read incoming data into the shtpData array
		for (int i = 0; i < dataLength; i++) {
			int incoming = spiDevice.write((byte) 0xFF)[0];
			if (i < MAX_PACKET_SIZE) {
				shtpData[i] = incoming;
			}
		}
		csGpio.setState(PinState.HIGH); // Release BNO080
		return true; // we are done

	}

	private boolean sendPacket(int channelNumber, int dataLength) throws InterruptedException, IOException {
		int packetLength = dataLength + SHTP_HEADER_SIZE;

		// Wait for BNO080 to indicate it is available for communication
		if (!waitForSPI()) {
			System.out.println("sendPacket something wrong");
			return false;
		}

		//BNO080 has max CLK of 3MHz, MSB first,
		//The BNO080 uses CPOL = 1 and CPHA = 1. This is mode3
		csGpio.setState(PinState.LOW);
		spiDevice.write((byte) (packetLength & 0xFF)); // Packet length LSB
		spiDevice.write((byte) (packetLength >> 8)); // Packet length MSB
		spiDevice.write((byte) channelNumber); // Channel Number
		// Send the sequence number, increments with each packet sent, different counter
		// for each channel
		spiDevice.write((byte) (sequenceNumber[channelNumber]++));
		for (int i = 0; i < dataLength; i++) {
			byte[] writeOut = spiDevice.write((byte) (shtpData[i] & 0xFF));
			System.out.println("sendPacket writeOut size: " + writeOut.length + "," + new String(writeOut) );
			shtpData[i] = writeOut[0];
		}
		csGpio.setState(PinState.HIGH);
		return true;
	}

	public void action() throws InterruptedException, IOException {
		init();

		System.out.println("ACTION");
		boolean state = waitForSPI();
		System.out.println("ACTION SPI STATE: " + state);

		//Turn on SPI Hardware
		boolean beginState = begin();
		System.out.println("action begin: " + beginState);

		/*
		 * At system startup, the hub must send its full advertisement message (see 5.2
		 * and 5.3) to the host. It must not send any other data until this step is
		 * complete. When BNO080 first boots it broadcasts big startup packet Read it
		 * and dump it
		 */
		waitForSPI(); // Wait for assertion of INT before reading advert message.
		receivePacket();

		/*
		 * The BNO080 will then transmit an unsolicited Initialize Response (see
		 * 6.4.5.2) Read it and dump it
		 */
		waitForSPI(); // Wait for assertion of INT before reading advert message.
		receivePacket();

		// Check communication with device
		shtpData[0] = SHTP_REPORT_PRODUCT_ID_REQUEST; // Request the product ID and reset info
		shtpData[1] = 0; // Reserved

		// Transmit packet on channel 2, 2 bytes
		sendPacket(CHANNEL_CONTROL, 2);

		// Now we wait for response
		waitForSPI();
		if (receivePacket()) {
			if (shtpData[0] == SHTP_REPORT_PRODUCT_ID_RESPONSE) {
				System.out.println("RECEIVED");
			}
		}

	}

	private void parseCommandReport() {

	}

	private boolean begin() throws InterruptedException, IOException {

		// Begin by resetting the IMU
		softReset();

		shtpData[0] = SHTP_REPORT_PRODUCT_ID_REQUEST; // Request the product ID and reset info
		shtpData[1] = 0; // Reserved

		sendPacket(CHANNEL_CONTROL, 2);

		// Now we wait for response
		if (receivePacket()) {
			if ((shtpData[0] & 0xFF) == SHTP_REPORT_PRODUCT_ID_RESPONSE) {
				System.out.println("Begin good");
				return true;
			}
		}

		System.out.println("begin: something went wrong");
		return false;
	}

}
