/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
import com.robo4j.hw.rpi.i2c.adafruitbackpack.BiColor8x8MatrixDevice;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.MatrixRotation;

/**
 * Adafruit Bi-Color 8x8 Matrix
 *
 * https://learn.adafruit.com/adafruit-led-backpack/bi-color-8x8-matrix
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Adafruit8x8MatrixUnit extends AbstractI2CBackpackUnit {

	public static final String DEFAULT_MATRIX_ROTATION = "DEFAULT_X_Y";
	public static final String ATTRIBUTE_ROTATION = "rotation";

	private BiColor8x8MatrixDevice device;

	public Adafruit8x8MatrixUnit(RoboContext context, String id) {
		super(DrawMessage.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		super.onInitialization(configuration);
		int brightness = configuration.getInteger(ATTRIBUTE_BRIGHTNESS, AbstractBackpack.DEFAULT_BRIGHTNESS);
		MatrixRotation rotation = MatrixRotation
				.valueOf(configuration.getString(ATTRIBUTE_ROTATION, DEFAULT_MATRIX_ROTATION).toUpperCase());
		try {
			device = new BiColor8x8MatrixDevice(getBus(), getAddress(), brightness);
		} catch (IOException e) {
			throw new ConfigurationException("Failed to instantiate device", e);
		}
		device.setRotation(rotation);
	}

	@Override
	public void onMessage(DrawMessage message) {
		processMessage(device, message);
	}

	@Override
	void paint(DrawMessage message) {
		device.drawPixels(message.getXs(), message.getYs(), message.getColors());
	}
}
