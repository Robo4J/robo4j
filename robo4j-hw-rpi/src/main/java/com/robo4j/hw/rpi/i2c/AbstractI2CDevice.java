/*
 * Copyright (C) 2014-2016, Marcus Hirt
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
package com.robo4j.hw.rpi.i2c;

import java.io.IOException;
import java.util.logging.Logger;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * Abstract super class for I2C devices.
 * 
 * @author Marcus Hirt
 */
public abstract class AbstractI2CDevice {
	private final int bus;
	private final int address;
	protected final I2CDevice i2cDevice;

	/**
	 * Creates an I2C device.
	 * 
	 * @param bus
	 *            the I2C bus to use.
	 * @param address
	 *            the address to use.
	 * 
	 * @see I2CBus
	 * 
	 * @throws IOException
	 *             if there was communication problem
	 * @throws UnsupportedBusNumberException
	 */
	public AbstractI2CDevice(int bus, int address) throws IOException {
		this.bus = bus;
		this.address = address;
		try {
			this.i2cDevice = I2CFactory.getInstance(bus).getDevice(address);
		} catch (com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException e) {
			throw new IOException("Unsupported bus", e);
		}
	}

	/**
	 * Returns the bus used when communicating with this I2C device.
	 * 
	 * @return the bus used when communicating with this I2C device.
	 */
	public final int getBus() {
		return bus;
	}

	/**
	 * Returns the address used when communicating with this I2C device.
	 * 
	 * @return the address used when communicating with this I2C device.
	 */
	public final int getAddress() {
		return address;
	}

	protected void writeByte(int address, byte b) throws IOException {
		i2cDevice.write(address, b);
	}

	protected int readByte(int address) throws IOException {
		return i2cDevice.read(address);
	}

	protected void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// Don't care
		}
	}

	/**
	 * Convenience method to get a logger for the specific class.
	 * 
	 * @return a logger for this class.
	 */
	public Logger getLogger() {
		return Logger.getLogger(this.getClass().getName());
	}

	/**
	 * Read 2 bytes unsigned.
	 */
	protected int readU2(int address) throws IOException {
		int hi = i2cDevice.read(address);
		int lo = i2cDevice.read(address + 1);
		return (hi << 8) + lo & 0xff;
	}

	/**
	 * Reads 2 bytes unsigned directly from the i2cDevice.
	 */
	protected int readU2Array() throws IOException {
		byte[] result = new byte[2];
		i2cDevice.read(result, 0, 2);
		return (result[0] << 8) + (result[1] & 0xff);
	}
	
	/**
	 * Read 2 bytes unsigned, array version.
	 */
	protected int readU2Array(int address) throws IOException { 
	    // FIXME(Marcus/Dec 20, 2016): Need to check which methods is
	    // superior. Tmp allocation vs fewer read calls?
		byte[] result = new byte[2];
		i2cDevice.read(address, result, 0, 2);
		return (result[0] << 8) + (result[1] & 0xff);
	}

	/**
	 * Read 3 bytes unsigned.
	 */
	protected int readU3(int address) throws IOException {
		// TODO: Check if there is any potential performance benefit to reading
		// them all at once into a byte array. It's probably translated to
		// to consecutive byte reads anyways, so probably not.
		int msb = i2cDevice.read(address);
		int lsb = i2cDevice.read(address + 1);
		int xlsb = i2cDevice.read(address + 2);
		return (msb << 16) + (lsb << 8) + xlsb & 0xff;
	}

	/**
	 * Read 3 bytes unsigned.
	 */
	protected int readU3Array(int address) throws IOException {
		// TODO: Check if there is any potential performance benefit to reading
		// them all at once into a byte array. It's probably translated to
		// to consecutive byte reads anyways, so probably not.
		byte[] result = new byte[3];
		i2cDevice.read(address, result, 0, 3);
		return (result[0] << 16) + (result[1] << 8) + result[2] & 0xff;
	}

}
