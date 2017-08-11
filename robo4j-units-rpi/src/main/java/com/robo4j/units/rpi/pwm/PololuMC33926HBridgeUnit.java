/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.units.rpi.pwm;

import java.io.IOException;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.robo4j.core.ConfigurationException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.hw.rpi.i2c.pwm.PWMPCA9685Device;
import com.robo4j.hw.rpi.i2c.pwm.PololuMC33926HBridgeEngine;
import com.robo4j.units.rpi.I2CEndPoint;
import com.robo4j.units.rpi.I2CRegistry;
import com.robo4j.units.rpi.I2CRoboUnit;

/**
 * Motor unit associated with the {@link PololuMC33926HBridgeEngine} driver.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class PololuMC33926HBridgeUnit extends I2CRoboUnit<Float> {
	/**
	 * The key used to configure which channel to use.
	 */
	public static final String CONFIGURATION_KEY_CHANNEL = "channel";

	/**
	 * The key used to configure the symbolic name.
	 */
	public static final String CONFIGURATION_KEY_NAME = "name";

	/**
	 * The key used to configure the gpio RaspiPin to use for "IN1". Use the
	 * name, such as GPIO_01.
	 */
	public static final String CONFIGURATION_KEY_GPIO_IN_1 = "in1";

	/**
	 * The key used to configure the gpio pin to use for "IN2". Use the name,
	 * such as GPIO_02.
	 */
	public static final String CONFIGURATION_KEY_GPIO_IN_2 = "in2";

	/**
	 * The key used to invert the direction of the motor. The value if the key
	 * is not present defaults to false.
	 */
	public static final String CONFIGURATION_KEY_INVERT = "invert";

	/**
	 * The engine.
	 */
	private PololuMC33926HBridgeEngine engine;

	public PololuMC33926HBridgeUnit(RoboContext context, String id) {
		super(Float.class, context, id);
	}

	/**
	 *
	 * @param configuration
	 *            unit configuration
	 * @throws ConfigurationException
	 */
	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		super.onInitialization(configuration);
		Object pwmDevice = I2CRegistry.getI2CDeviceByEndPoint(new I2CEndPoint(getBus(), getAddress()));
		PWMPCA9685Device pcaDevice = null;
		try {
			if (pwmDevice == null) {
				pcaDevice = new PWMPCA9685Device(getBus(), getAddress());
				I2CRegistry.registerI2CDevice(pcaDevice, new I2CEndPoint(getBus(), getAddress()));
				pcaDevice.setPWMFrequency(50);
			} else {
				pcaDevice = (PWMPCA9685Device) pwmDevice;
			}
		} catch (IOException e) {
			throw new ConfigurationException("Could not initialize hardware", e);
		}
		int channel = configuration.getInteger(CONFIGURATION_KEY_CHANNEL, -1);
		if (channel == -1) {
			throw ConfigurationException.createMissingConfigNameException(CONFIGURATION_KEY_CHANNEL);
		}

		String in1Name = configuration.getString(CONFIGURATION_KEY_GPIO_IN_1, null);
		if (in1Name == null) {
			throw ConfigurationException.createMissingConfigNameException(CONFIGURATION_KEY_GPIO_IN_1);
		}
		Pin in1 = RaspiPin.getPinByName(in1Name);

		String in2Name = configuration.getString(CONFIGURATION_KEY_GPIO_IN_1, null);
		if (in2Name == null) {
			throw ConfigurationException.createMissingConfigNameException(CONFIGURATION_KEY_GPIO_IN_1);
		}
		Pin in2 = RaspiPin.getPinByName(in2Name);

		boolean invert = configuration.getBoolean(CONFIGURATION_KEY_INVERT, false);

		engine = new PololuMC33926HBridgeEngine(configuration.getString(CONFIGURATION_KEY_NAME, "MC33926"), pcaDevice.getChannel(channel),
				in1, in2, invert);
	}

	@Override
	public void onMessage(Float message) {
		try {
			engine.setSpeed(message);
		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(), "Failed to set motor speed to " + message, e);
		}
	}
}
