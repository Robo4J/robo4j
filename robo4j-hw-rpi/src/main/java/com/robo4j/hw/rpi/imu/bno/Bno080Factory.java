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
package com.robo4j.hw.rpi.imu.bno;

import java.io.IOException;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiMode;
import com.robo4j.hw.rpi.imu.bno.Bno055Device.OperatingMode;
import com.robo4j.hw.rpi.imu.bno.impl.Bno080SPIDevice;

/**
 * Factory for creating BNO080 devices
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class Bno080Factory {
	/**
	 * Will create an I2C connected BNO080 device.
	 * 
	 * NOTE(Marcus/Sept 01, 2019): Note that due to the Raspberry Pi not
	 * properly supporting clock stretching, using the BNO080 with I2C on the
	 * RaspberryPi will not work (yet).
	 * 
	 * @return an I2C connected BNO080Device
	 * @throws IOException
	 *             exception
	 */
	public static Bno080Device createDefaultI2CDevice() throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	/**
	 * Will create a BNO080 device connected over serial.
	 * 
	 * @return a serial connected BNO80 device.
	 * @throws IOException
	 *             exception
	 */
	public static Bno080Device createDefaultSerialDevice() throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	/**
	 * Will create an I2C connected BNO080 device.
	 * 
	 * @param bus
	 *            the I2C bus to use.
	 * @param address
	 *            the I2C address to use.
	 * @param operatingMode
	 *            the (initial) operating mode.
	 * @return I2C connected BNO080Device
	 * @throws IOException
	 *             exception
	 */
	public static Bno080Device createDevice(int bus, int address, OperatingMode operatingMode) throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	/**
	 * Will create a BNO080 device connected over serial.
	 * 
	 * @param serialPort
	 *            the serial port to use.
	 * @param operatingMode
	 *            the (initial) operating mode to use.
	 * @param retryTimeout
	 *            the timeout on buffer overruns before trying again.
	 * @param noOfRetries
	 *            the number of times to retry.
	 * @return a serial connected BNO80 device.
	 * @throws IOException
	 *             exception
	 */
	public static Bno080Device createDevice(String serialPort, OperatingMode operatingMode, long retryTimeout, int noOfRetries)
			throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	/**
	 * Will create a BNO080 device connected over SPI using the default
	 * settings.
	 * 
	 * @return an SPI connected BNO80 device.
	 * @throws IOException
	 *             exception
	 * @throws InterruptedException
	 */
	public static Bno080Device createDefaultSPIDevice() throws IOException, InterruptedException {
		return new Bno080SPIDevice();
	}

	/**
	 * Will create a BNO080 device connected over SPI using the provided
	 * settings.
	 * 
	 * @return an SPI connected BNO80 device.
	 * @throws IOException
	 *             exception
	 * @throws InterruptedException 
	 */
	public static Bno080Device createDefaultSPIDevice(SpiChannel channel, SpiMode mode, int speed, Pin wake, Pin cs, Pin reset, Pin interrupt) throws IOException, InterruptedException {
		return new Bno080SPIDevice(channel, mode, speed, wake, cs, reset, interrupt);
	}

}
