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
package com.robo4j.units.rpi.roboclaw;

import java.io.IOException;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.i2c.pwm.PCA9685Servo;
import com.robo4j.hw.rpi.i2c.pwm.PWMPCA9685Device;
import com.robo4j.hw.rpi.pwm.roboclaw.RoboClawRCTank;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.units.rpi.I2CRegistry;
import com.robo4j.units.rpi.I2CRoboUnit;

/**
 * Configurable unit for a RoboClaw configured with two engines, controlled
 * using PWM signals from a PCA9685.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboClawRCTankUnit extends I2CRoboUnit<MotionEvent> {
	/**
	 * The key used to configure which channel to use for the left engine.
	 */
	public static String CONFIGURATION_KEY_LEFT_CHANNEL = "leftChannel";

	/**
	 * The key used to configure if the channel for the left engine needs to be
	 * inverted.
	 */
	public static String CONFIGURATION_KEY_LEFT_INVERTED = "leftInverted";

	/**
	 * The key used to configure which channel to use for the right engine.
	 */
	public static String CONFIGURATION_KEY_RIGHT_CHANNEL = "rightChannel";

	/**
	 * The key used to configure if the channel for the left engine needs to be
	 * inverted.
	 */
	public static String CONFIGURATION_KEY_RIGHT_INVERTED = "rightInverted";

	private RoboClawRCTank tank;

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            the RoboContext in which to define the unit
	 * @param id
	 *            the id of the unit
	 */
	public RoboClawRCTankUnit(RoboContext context, String id) {
		super(MotionEvent.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		super.onInitialization(configuration);
		PWMPCA9685Device pcaDevice = I2CRegistry.createAndRegisterIfAbsent(getBus(), getAddress(),
				() -> PWMPCA9685Device.createDevice(getBus(), getAddress()));
		int leftChannel = configuration.getInteger(CONFIGURATION_KEY_LEFT_CHANNEL, -1);
		if (leftChannel == -1) {
			throw ConfigurationException.createMissingConfigNameException(CONFIGURATION_KEY_LEFT_CHANNEL);
		}
		int rightChannel = configuration.getInteger(CONFIGURATION_KEY_RIGHT_CHANNEL, -1);
		if (rightChannel == -1) {
			throw ConfigurationException.createMissingConfigNameException(CONFIGURATION_KEY_RIGHT_CHANNEL);
		}
		boolean leftInvert = configuration.getBoolean(CONFIGURATION_KEY_LEFT_INVERTED, false);
		boolean rightInvert = configuration.getBoolean(CONFIGURATION_KEY_RIGHT_INVERTED, false);

		PCA9685Servo leftServo = new PCA9685Servo(pcaDevice.getChannel(leftChannel));
		leftServo.setInverted(leftInvert);
		PCA9685Servo rightServo = new PCA9685Servo(pcaDevice.getChannel(rightChannel));
		rightServo.setInverted(rightInvert);
		try {
			tank = new RoboClawRCTank(leftServo, rightServo);
		} catch (IOException e) {
			throw new ConfigurationException("Could not initiate device!", e);
		}
	}

	@Override
	public void onMessage(MotionEvent message) {
		super.onMessage(message);
		try {
			tank.setDirection(message.getDirection());
			tank.setSpeed(message.getSpeed());
		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(), "Could not set speed and/or direction to " + message, e);
		}
	}
}
