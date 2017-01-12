/*
 * Copyright (C) 2016. Miroslav Wengner and Marcus Hirt
 * This RpiDevice.java  is part of robo4j.
 * module: robo4j-rpi
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.rpi.motor;

import java.io.Closeable;
import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

/**
 * @author Miro Kopecky (@miragemiko)
 * @since 19.12.2016
 */
public class RpiDevice implements Closeable {

	protected I2CBus bus;
	protected I2CDevice device;

	public RpiDevice() {
	}

	public void setBus(I2CBus bus) {
		this.bus = bus;
	}

	public void setDevice(int address) throws IOException {
		this.device = bus.getDevice(address);
	}

	@Override
	public void close() throws IOException {
		bus.close();
	}
}
