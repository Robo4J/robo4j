/*
 * Copyright (C) 2014-2017. Miroslav Wengner, Marcus Hirt
 * This LCDFactory.java  is part of robo4j.
 * module: robo4j-hw-rpi
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
package com.robo4j.hw.rpi.i2c.adafruitlcd;

import java.io.IOException;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.robo4j.hw.rpi.i2c.adafruitlcd.impl.RealLcd;
import com.robo4j.hw.rpi.i2c.adafruitlcd.mockup.MockupLcd;
/**
 * FIXME(Marcus/Dec 20, 2016): We should probably not let the mockup etc be part of the standard API.
 * Let's keep it clean?
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 01.11.2016
 */
public final class LcdFactory {
	/**
	 * Set this system property to true to make the factory return a Swing mockup
	 * instead of an i2c LCD device.
	 */
	public static final String SYSTEM_PROPERTY_MOCK = "com.robo4j.hw.rpi.i2c.adafruitlcd.mock";

	public static AdafruitLcd createLCD() throws IOException, UnsupportedBusNumberException {
		if (Boolean.getBoolean(SYSTEM_PROPERTY_MOCK)) {
			return new MockupLcd();
		}
		return new RealLcd();
	}

	public static AdafruitLcd createLCD(int bus, int address) throws IOException, UnsupportedBusNumberException {
		if (Boolean.getBoolean(SYSTEM_PROPERTY_MOCK)) {
			return new MockupLcd(bus, address);
		}
		return new RealLcd(bus, address);
	}

}
