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
package com.robo4j.hw.rpi.i2c.adafruitlcd;

import java.io.IOException;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.robo4j.hw.rpi.i2c.adafruitlcd.impl.AdafruitLcdImpl;
import com.robo4j.hw.rpi.i2c.adafruitlcd.mockup.AdafruitLcdMockup;
/**
 * FIXME(Marcus/Dec 20, 2016): We should probably not let the mockup etc be part of the standard API.
 * FIXME(Miro/Jan 29, 2017): we should move it to the test
 * Let's keep it clean?
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class LcdFactory {
	/**
	 * Set this system property to true to make the factory return a Swing mockup
	 * instead of an i2c LCD device.
	 */
	private  static final String SYSTEM_PROPERTY_MOCK = "com.robo4j.hw.rpi.i2c.adafruitlcd.mock";

	public static AdafruitLcd createLCD() throws IOException, UnsupportedBusNumberException {
		if (Boolean.getBoolean(SYSTEM_PROPERTY_MOCK)) {
			return new AdafruitLcdMockup();
		}
		return new AdafruitLcdImpl();
	}

	public static AdafruitLcd createLCD(int bus, int address) throws IOException, UnsupportedBusNumberException {
		if (Boolean.getBoolean(SYSTEM_PROPERTY_MOCK)) {
			return new AdafruitLcdMockup(bus, address);
		}
		return new AdafruitLcdImpl(bus, address);
	}

}
