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
package com.robo4j.hw.rpi.imu;

import java.io.IOException;

import com.robo4j.hw.rpi.imu.BNO055Device.OperatingMode;
import com.robo4j.hw.rpi.imu.impl.BNO055I2CDevice;
import com.robo4j.hw.rpi.imu.impl.BNO055SerialDevice;

/**
 * Factory for creating BNO055 devices
 * 
 * @author Marcus
 */
public final class BNO055Factory {
	/**
	 * Will create an I2C connected BNO055 device.
	 * 
	 * NOTE(Marcus/Jul 30, 2017): Note that due to the Raspberry Pi not properly
	 * supporting clock stretching, using the BNO055 with I2C on the RaspberryPi
	 * will not work (yet). (Tried with Raspberry Pi 3.)
	 * 
	 * @return an I2C connected BNO055Device
	 * @throws IOException
	 */
	public static BNO055Device createDefaultI2CDevice() throws IOException {
		return new BNO055I2CDevice();
	}

	/**
	 * Will create a BNO055 device connected over serial.
	 * 
	 * @return a serial connected BNO55 device.
	 * @throws IOException
	 */
	public static BNO055Device createDefaultSerialDevice() throws IOException {
		return new BNO055SerialDevice();
	}

	public static BNO055Device createDevice(int bus, int address, OperatingMode operatingMode) throws IOException {
		return new BNO055I2CDevice(bus, address, operatingMode);
	}

	public static BNO055Device createDevice(String serialPort, OperatingMode operatingMode) throws IOException {
		return new BNO055SerialDevice(serialPort, operatingMode);
	}

}
