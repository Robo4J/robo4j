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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.units.rpi.pwm;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.i2c.pwm.PCA9685Servo;
import com.robo4j.hw.rpi.i2c.pwm.PWMPCA9685Device;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.units.rpi.I2CRegistry;
import com.robo4j.units.rpi.I2CRoboUnit;

/**
 * Servo unit associated with the PCA9685 PWM driver.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class PCA9685ServoUnit extends I2CRoboUnit<Float> {
	/**
	 * The key used to configure which channel to use.
	 */
	public static final String CONFIGURATION_KEY_CHANNEL = "channel";
	/**
	 * The key used to configure how much trim to use.
	 */
	public static final String CONFIGURATION_KEY_TRIM = "trim";
	/**
	 * The key used to configure if the servo should be inverted.
	 */
	public static final String CONFIGURATION_KEY_INVERTED = "inverted";
	/**
	 * The key used to configure the dual rate to use.
	 */
	public static final String CONFIGURATION_KEY_DUAL_RATE = "dualRate";
	/**
	 * The key used to configure the expo to use.
	 */
	public static final String CONFIGURATION_KEY_EXPO = "expo";
	/**
	 * The setting to reset to on shutdown. If this is not set, nothing will
	 * happen on shutdown.
	 */
	public static final String CONFIGURATION_KEY_SHUTDOWN_VALUE = "shutdownValue";

	public static final AttributeDescriptor<Float> ATTRIBUTE_SERVO_INPUT = DefaultAttributeDescriptor.create(Float.class, "input");
	public static final Collection<AttributeDescriptor<?>> KNOWN_ATTRIBUTES = Collections.singleton(ATTRIBUTE_SERVO_INPUT);

	private PCA9685Servo servo;
	private Integer channel;
	private Float shutdownValue;

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            the RoboContext in which to define the unit
	 * @param id
	 *            the id of the unit
	 */
	public PCA9685ServoUnit(RoboContext context, String id) {
		super(Float.class, context, id);
	}

	/**
	 *
	 * @param configuration
	 *            unit configuration
	 * @throws ConfigurationException
	 *             exception
	 */
	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		super.onInitialization(configuration);
		PWMPCA9685Device pcaDevice = I2CRegistry.createAndRegisterIfAbsent(getBus(), getAddress(),
				() -> PWMPCA9685Device.createDevice(getBus(), getAddress()));
		channel = configuration.getInteger(CONFIGURATION_KEY_CHANNEL, -1);
		if (channel == -1) {
			throw ConfigurationException.createMissingConfigNameException(CONFIGURATION_KEY_CHANNEL);
		}
		servo = new PCA9685Servo(pcaDevice.getChannel(channel));
		servo.setTrim(configuration.getFloat(CONFIGURATION_KEY_TRIM, 0f));
		servo.setInverted(configuration.getBoolean(CONFIGURATION_KEY_INVERTED, false));
		servo.setDualRate(configuration.getFloat(CONFIGURATION_KEY_DUAL_RATE, 1.0f));
		servo.setExpo(configuration.getFloat(CONFIGURATION_KEY_EXPO, 0.0f));
		shutdownValue = configuration.getFloat(CONFIGURATION_KEY_SHUTDOWN_VALUE, null);
	}

	/**
	 * -1 to 1
	 * 
	 * @param message
	 *            the message received by this unit.
	 * 
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

	@Override
	public void shutdown() {
		if (shutdownValue != null) {
			try {
				servo.setInput(shutdownValue.floatValue());
			} catch (IOException e) {
				SimpleLoggingUtil.debug(PCA9685ServoUnit.class, "Failed to set the shutdown value!", e);
			}
		}
		super.shutdown();
	}
}
