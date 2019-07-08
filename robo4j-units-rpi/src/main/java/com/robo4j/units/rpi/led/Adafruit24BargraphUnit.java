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
import com.robo4j.hw.rpi.i2c.adafruitbackpack.BiColor24BarDevice;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.LEDBackpack;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.LEDBackpackType;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.PackElement;

import java.util.List;

/**
 * Adafruit Bi-Color 24 Bargraph Unit This version of the LED backpack is
 * designed for these bright and colorful bi-color bargraph modules. Each module
 * has 12 red and 12 green LEDs inside, for a total of 24 LEDs controlled as a
 * 1x12 matrix. We put two modules on each backpack for a 24-bar long bargraph
 * (48 total LEDs)
 *
 * https://learn.adafruit.com/adafruit-led-backpack/bi-color-24-bargraph
 *
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Adafruit24BargraphUnit extends AbstractI2CBackpackUnit<BiColor24BarDevice> {

	public Adafruit24BargraphUnit(RoboContext context, String id) {
		super(LEDBackpackMessage.class, context, id);
	}

	private BiColor24BarDevice device;

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		Integer address = configuration.getInteger(ATTRIBUTE_ADDRESS, null);
		Integer bus = configuration.getInteger(ATTRIBUTE_BUS, null);
		validateConfiguration(address, bus);
		int brightness = configuration.getInteger(ATTRIBUTE_BRIGHTNESS, LEDBackpack.DEFAULT_BRIGHTNESS);

		device = getBackpackDevice(LEDBackpackType.BI_COLOR_BAR_24, bus, address, brightness);
	}

	@Override
	public void onMessage(LEDBackpackMessage message) {
		processMessage(device, message);
	}

	@Override
	void addElements(List<PackElement> elements) {
		int size = elements.size() > BiColor24BarDevice.MAX_BARS ? BiColor24BarDevice.MAX_BARS : elements.size();
		for (int i = 0; i < size; i++) {
			PackElement element = elements.get(i);
			device.addBar(element);
		}
	}
}
