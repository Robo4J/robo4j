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
import java.util.Collection;
import java.util.Collections;

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.ConfigurationException;
import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.RoboContext;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.hw.rpi.i2c.pwm.PWMPCA9685Device;
import com.robo4j.hw.rpi.i2c.pwm.Servo;
import com.robo4j.units.rpi.I2CEndPoint;
import com.robo4j.units.rpi.I2CRegistry;
import com.robo4j.units.rpi.lcd.I2CRoboUnit;

/**
 * Servo unit associated with the PCA9685 PWM driver.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class PCA9685ServoUnit extends I2CRoboUnit<Float> {
	public static String CONFIGURATION_KEY_CHANNEL = "channel";
	public static String CONFIGURATION_KEY_TRIM = "trim";
	public static String CONFIGURATION_KEY_INVERTED = "inverted";
	public static String CONFIGURATION_KEY_DUAL_RATE = "dualRate";
	public static String CONFIGURATION_KEY_EXPO = "expo";

	public static Collection<AttributeDescriptor<?>> KNOWN_ATTRIBUTES = Collections.unmodifiableCollection(
			Collections.singleton(DefaultAttributeDescriptor.create(Float.class, "input") ));

	private Servo servo;
	private Integer channel;

	public PCA9685ServoUnit(RoboContext context, String id) {
		super(Float.class, context, id);
	}

	/**
	 *
	 * @param configuration
	 *            - unit configuration
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
		channel = configuration.getInteger(CONFIGURATION_KEY_CHANNEL, -1);
		if (channel == -1) {
			throw ConfigurationException.createMissingConfigNameException(CONFIGURATION_KEY_CHANNEL);
		}
		servo = new Servo(pcaDevice.getChannel(channel));
		servo.setTrim(configuration.getFloat(CONFIGURATION_KEY_TRIM, 0f));
		servo.setInverted(configuration.getBoolean(CONFIGURATION_KEY_INVERTED, false));
		servo.setDualRate(configuration.getFloat(CONFIGURATION_KEY_DUAL_RATE, 1.0f));
		servo.setExpo(configuration.getFloat(CONFIGURATION_KEY_EXPO, 0.0f));
	}

	/**
	 * -1 to 1
	 * 
	 * @param message
	 *            the message received by this unit.
	 * 
	 * @return the unit specific result from the call.
	 */
	@Override
	public void onMessage(Float message) {
		try {
			servo.setInput(message);
		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(), "Could not set servo input", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
		if (descriptor.getAttributeName().equals("input") && descriptor.getAttributeType() == Float.class) {
			try {
				return (R) Float.valueOf(servo.getInput());
			} catch (IOException e) {
				SimpleLoggingUtil.error(getClass(), "Failed to read servo input", e);
			}
		}
		return super.onGetAttribute(descriptor);
	}

	@Override
	public Collection<AttributeDescriptor<?>> getKnownAttributes() {
		return KNOWN_ATTRIBUTES; 
	}
}
