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
import com.robo4j.RoboUnit;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.LEDBackpack;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.LEDBackpackFactory;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.LEDBackpackType;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.PackElement;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.units.rpi.I2CEndPoint;
import com.robo4j.units.rpi.I2CRegistry;

import java.util.List;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
abstract class AbstractI2CBackpackUnit<T extends LEDBackpack> extends RoboUnit<LEDBackpackMessage> {

	static final String ATTRIBUTE_ADDRESS = "address";
	static final String ATTRIBUTE_BUS = "bus";
	static final String ATTRIBUTE_BRIGHTNESS = "brightness";

	AbstractI2CBackpackUnit(Class<LEDBackpackMessage> messageType, RoboContext context, String id) {
		super(messageType, context, id);
	}

	void validateConfiguration(Integer address, Integer bus) throws ConfigurationException {
		if (address == null) {
			throw new ConfigurationException(ATTRIBUTE_ADDRESS);
		}
		if (bus == null) {
			throw new ConfigurationException(ATTRIBUTE_BUS);
		}
	}

	@SuppressWarnings("unchecked")
	T getBackpackDevice(LEDBackpackType type, int bus, int address, int brightness) throws ConfigurationException {
		I2CEndPoint endPoint = new I2CEndPoint(bus, address);
		Object device = I2CRegistry.getI2CDeviceByEndPoint(endPoint);
		if (device == null) {
			try {
				device = LEDBackpackFactory.createDevice(bus, address, type, brightness);
				// Note that we cannot catch hardware specific exceptions here,
				// since they will be loaded when we run as mocked.
			} catch (Exception e) {
				throw new ConfigurationException(e.getMessage());
			}
			I2CRegistry.registerI2CDevice(device, new I2CEndPoint(bus, address));
		}
		return (T) device;
	}

	void processMessage(LEDBackpack device, LEDBackpackMessage message) {
		switch (message.getType()) {
		case CLEAR:
			device.clear();
			break;
		case ADD:
			addElements(message.getElements());
			break;
		case DISPLAY:
			addElements(message.getElements());
			device.display();
			break;
		default:
			SimpleLoggingUtil.error(getClass(), String.format("Illegal message: %s", message));

		}
	}

	abstract void addElements(List<PackElement> elements);
}
