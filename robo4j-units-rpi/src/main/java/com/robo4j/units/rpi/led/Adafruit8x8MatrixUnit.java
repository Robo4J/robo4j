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

package com.robo4j.units.rpi.led;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.BiColor8x8MatrixDevice;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.AbstractLedBackpack;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.LedBackpackType;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.MatrixRotation;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.XYElement;

import java.util.List;

/**
 * Adafruit Bi-Color 8x8 Matrix
 *
 * https://learn.adafruit.com/adafruit-led-backpack/bi-color-8x8-matrix
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Adafruit8x8MatrixUnit extends AbstractI2CBackpackUnit<BiColor8x8MatrixDevice, XYElement> {

	public static final String DEFAULT_MATRIX_ROTATION = "DEFAULT_X_Y";
	public static final String ATTRIBUTE_ROTATION = "rotation";

	private BiColor8x8MatrixDevice device;

	public Adafruit8x8MatrixUnit(RoboContext context, String id) {
		super(LedBackpackMessages.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		Integer address = configuration.getInteger(ATTRIBUTE_ADDRESS, null);
		Integer bus = configuration.getInteger(ATTRIBUTE_BUS, null);
		validateConfiguration(address, bus);
		int brightness = configuration.getInteger(ATTRIBUTE_BRIGHTNESS, AbstractLedBackpack.DEFAULT_BRIGHTNESS);
		MatrixRotation rotation = MatrixRotation
				.valueOf(configuration.getString(ATTRIBUTE_ROTATION, DEFAULT_MATRIX_ROTATION).toUpperCase());
		device = getBackpackDevice(LedBackpackType.BI_COLOR_MATRIX_8x8, bus, address, brightness);
		device.setRotation(rotation);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void onMessage(LedBackpackMessages message) {
		processMessage(device, message);
	}

	@Override
	void addElements(List<XYElement> elements) {
		device.addPixels(elements);
	}
}
