/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.hw.rpi.i2c.lidar;

import java.io.IOException;

import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;
import com.robo4j.hw.rpi.utils.I2cBus;

/**
 * Abstraction for the Garmin/PulsedLight LidarLite device.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class LidarLiteDevice extends AbstractI2CDevice {
	// 0x62 is the default address of the Lidar-Lite Device
	// This can be changed (see the operation manual)
	private static final int DEFAULT_I2C_ADDRESS = 0x62;
	private static final int REGISTER_COMMAND = 0x0;
	private static final int REGISTER_RESULT = 0x8f;
	private static final byte COMMAND_ACQUIRE_RANGE = 0x4;


	/**
	 * Constructs a LidarDevice using the default settings. (I2CBUS.BUS_1, 0x62)
	 * 
	 * @throws IOException
	 *             if there was communication problem
	 */
	public LidarLiteDevice() throws IOException {
		super(I2cBus.BUS_1, DEFAULT_I2C_ADDRESS);
	}

	/**
	 * Creates a software interface to a Lidar-Lite.
	 * 
	 * @param bus
	 *            the I2C bus to use.
	 * @param address
	 *            the address to use.
	 * 
	 * @see I2cBus
	 * 
	 * @throws IOException
	 *             if there was communication problem
	 */
	public LidarLiteDevice(I2cBus bus, int address) throws IOException {
		super(bus, address);
	}

	/**
	 * Call this to acquire a new range reading.
	 * 
	 * @throws IOException
	 *             if there was communication problem
	 */
	public void acquireRange() throws IOException {
		//i2CConfig.write(REGISTER_COMMAND, COMMAND_ACQUIRE_RANGE);
		writeByte(REGISTER_COMMAND, COMMAND_ACQUIRE_RANGE);
	}

	/**
	 * Reads the result. Note that if the result is read too soon after a call
	 * to acquireRange, the result may not be ready yet, and the previously
	 * acquired result may be read. Check the documentation of your particular
	 * device to know for how long to wait!
	 * 
	 * @return the distance in m.
	 * @throws IOException
	 *             if there was communication problem
	 */
	public float readDistance() throws IOException {
		int inCM = readU2Array(REGISTER_RESULT);
		return inCM / 100.0f;
	}
}
