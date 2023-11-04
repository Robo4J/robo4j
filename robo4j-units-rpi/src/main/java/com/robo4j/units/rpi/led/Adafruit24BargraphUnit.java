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

package com.robo4j.units.rpi.led;

import java.io.IOException;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.AbstractBackpack;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.BiColor24BarDevice;

/**
 * Adafruit Bi-Color 24 Bargraph Unit This version of the LED backpack is
 * designed for these bright and colorful bi-color bargraph modules. Each module
 * has 12 red and 12 green LEDs inside, for a total of 24 LEDs controlled as a
 * 1x12 matrix. We put two modules on each backpack for a 24-bar long bargraph
 * (48 total LEDs)
 *
 * https://learn.adafruit.com/adafruit-led-backpack/bi-color-24-bargraph
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Adafruit24BargraphUnit extends AbstractI2CBackpackUnit {
	private BiColor24BarDevice device;

	public Adafruit24BargraphUnit(RoboContext context, String id) {
		super(DrawMessage.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		super.onInitialization(configuration);
		int brightness = configuration.getInteger(ATTRIBUTE_BRIGHTNESS, AbstractBackpack.DEFAULT_BRIGHTNESS);

		try {
			device = new BiColor24BarDevice(getBus(), getAddress(), brightness);
		} catch (IOException e) {
			throw new ConfigurationException("Failed to instantiate device", e);
		}
	}

	@Override
	public void onMessage(DrawMessage message) {
		processMessage(device, message);
	}

	@Override
	void paint(DrawMessage message) {
		for (int i = 0; i < message.getXs().length; i++) {
			device.setBar(message.getXs()[i], message.getColors()[i]);
		}
	}
}
