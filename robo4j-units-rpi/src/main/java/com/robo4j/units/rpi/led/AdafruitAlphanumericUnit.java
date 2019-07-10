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
import com.robo4j.hw.rpi.i2c.adafruitbackpack.AbstractLEDBackpack;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.AlphanumericDevice;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.AsciElement;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.LEDBackpackType;

import java.util.List;

/**
 * AdafruitAlphanumericUnit
 *
 * This version of the LED backpack is designed for two dual 14-segment
 * "Alphanumeric" displays. These 14-segment displays normally require 18 pins
 * (4 'characters' and 14 total segments each) This backpack solves the
 * annoyance of using 18 pins or a bunch of chips by having an I2C
 * constant-current matrix controller sit neatly on the back of the PCB. The
 * controller chip takes care of everything, drawing all the LEDs in the
 * background. All you have to do is write data to it using the 2-pin I2C
 * interface
 *
 * see https://learn.adafruit.com/adafruit-led-backpack/0-54-alphanumeric
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AdafruitAlphanumericUnit extends AbstractI2CBackpackUnit<AlphanumericDevice, AsciElement> {

	public AdafruitAlphanumericUnit(RoboContext context, String id) {
		super(LEDBackpackMessage.class, context, id);
	}

	private AlphanumericDevice device;

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		Integer address = configuration.getInteger(ATTRIBUTE_ADDRESS, null);
		Integer bus = configuration.getInteger(ATTRIBUTE_BUS, null);
		validateConfiguration(address, bus);
		int brightness = configuration.getInteger(ATTRIBUTE_BRIGHTNESS, AbstractLEDBackpack.DEFAULT_BRIGHTNESS);

		device = getBackpackDevice(LEDBackpackType.ALPHANUMERIC, bus, address, brightness);
	}

	@Override
	public void onMessage(LEDBackpackMessage message) {
		processMessage(device, message);
	}

	@Override
	void addElements(List<AsciElement> elements) {
		for (AsciElement e : elements) {
			if (e.getPosition() == null) {
				device.addCharacter(e.getValue(), e.getDot());
			} else {
				device.addCharacter(e.getPosition(), e.getValue(), e.getDot());
			}
		}
	}
}
