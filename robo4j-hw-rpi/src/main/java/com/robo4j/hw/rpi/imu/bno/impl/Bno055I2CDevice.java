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
package com.robo4j.hw.rpi.imu.bno.impl;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.robo4j.hw.rpi.i2c.ReadableDevice;
import com.robo4j.hw.rpi.imu.bno.Bno055Device;
import com.robo4j.math.geometry.Tuple3f;

import java.io.IOException;

/**
 * I2C implementation for the BN0055 absolute orientation device.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class Bno055I2CDevice extends AbstractBno055Device implements ReadableDevice<Tuple3f>, Bno055Device {
	private static final int DEFAULT_I2C_ADDRESS = 0x28;

	private final int bus;
	private final int address;
	protected final I2CDevice i2cDevice;

	/**
	 * Creates a BNO055Device with the default settings.
	 * 
	 * @throws IOException
	 *             exception
	 * 
	 * @see PowerMode
	 * @see OperatingMode
	 */
	public Bno055I2CDevice() throws IOException {
		this(I2CBus.BUS_1, DEFAULT_I2C_ADDRESS, OperatingMode.NDOF);
	}

	/**
	 * Creates a BNO055Device with the provided explicit settings.
	 * 
	 * @param bus
	 *            the i2c bus on which the BNO is.
	 * @param address
	 *            the address to which the BNO is configured.
	 * @param operatingMode
	 *            the {@link OperatingMode} to initialize to.
	 * @throws IOException
	 *             exception
	 */
	public Bno055I2CDevice(int bus, int address, OperatingMode operatingMode) throws IOException {
		this.bus = bus;
		this.address = address;
		try {
			this.i2cDevice = I2CFactory.getInstance(bus).getDevice(address);
		} catch (com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException e) {
			throw new IOException("Unsupported bus", e);
		}
		initialize(operatingMode);
	}

	@Override
	protected int read(int register) throws IOException {
		return i2cDevice.read(register);
	}

	@Override
	protected void write(int register, byte b) throws IOException {
		i2cDevice.write(register, b);
	}

	public int getBus() {
		return bus;
	}

	public int getAddress() {
		return address;
	}

	@Override
	protected byte[] read(int register, int length) throws IOException {
		byte[] data = new byte[length];
		i2cDevice.read(register, data, 0, length);
		return data;
	}

	@Override
	public void shutdown() {
	}
}
