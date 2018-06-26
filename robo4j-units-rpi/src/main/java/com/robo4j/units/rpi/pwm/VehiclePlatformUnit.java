/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.Servo;
import com.robo4j.hw.rpi.i2c.pwm.PCA9685Servo;
import com.robo4j.hw.rpi.i2c.pwm.PWMPCA9685Device;
import com.robo4j.hw.rpi.pwm.VehiclePlatform;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.units.rpi.I2CEndPoint;
import com.robo4j.units.rpi.I2CRegistry;
import com.robo4j.units.rpi.I2CRoboUnit;

import java.io.IOException;

/**
 * VehiclePlatformUnit is the example which control the vehicle platform with
 * two motors: * 1. throttle motor * 2. rotate servo
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class VehiclePlatformUnit extends I2CRoboUnit<VehicleEvent> {

	private static final int DEFAULT_CONF_INT_VALUE = -1;
	private static final String CONFIGURATION_KEY_SIGNAL_EQUILIBRIUM = "signalEquilibrium";
	private static final String CONFIGURATION_KEY_CHANNEL_THROTTLE = "channelThrottle";
	private static final String CONFIGURATION_KEY_CHANNEL_STEERING = "channelSteering";
	private static final String CONFIGURATION_KEY_CHANNEL_LEG = "channelLeg";
	private static final String CONFIGURATION_KEY_CHANNEL_SHIFT = "channelShift";
    /**
	 * SERVO values corresponds to the channel used on PWM PC9685
	 */
    private static final int SERVO_FREQUENCY = 250;
    private static final int SERVO_THROTTLE = 0;
    private static final int SERVO_STEERING = 5;
	private static final int SERVO_LEG = 6;
	private static final int SERVO_SHIFT = 7;

	private VehiclePlatform vehiclePlatform;

	public VehiclePlatformUnit(RoboContext context, String id) {
		super(VehicleEvent.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
        super.onInitialization(configuration);
        Object pwmDevice = I2CRegistry.getI2CDeviceByEndPoint(new I2CEndPoint(getBus(), getAddress()));
        PWMPCA9685Device device = PCA9685Utils.initPwmDevice(pwmDevice, getBus(), getAddress(), SERVO_FREQUENCY);

        int signalEquilibrium = configuration.getInteger(CONFIGURATION_KEY_SIGNAL_EQUILIBRIUM, DEFAULT_CONF_INT_VALUE);
        validateIntPropertyParameter(signalEquilibrium, CONFIGURATION_KEY_SIGNAL_EQUILIBRIUM);

        int channelThrottle = configuration.getInteger(CONFIGURATION_KEY_CHANNEL_THROTTLE, DEFAULT_CONF_INT_VALUE);
        validateIntPropertyParameter(channelThrottle, CONFIGURATION_KEY_CHANNEL_THROTTLE);
        Servo throttleServo = new PCA9685Servo(device.getChannel(channelThrottle));

        int channelSteering = configuration.getInteger(CONFIGURATION_KEY_CHANNEL_STEERING, DEFAULT_CONF_INT_VALUE);
        validateIntPropertyParameter(channelSteering, CONFIGURATION_KEY_CHANNEL_STEERING);
        Servo steeringServo = new PCA9685Servo(device.getChannel(channelSteering));

        int channelLeg = configuration.getInteger(CONFIGURATION_KEY_CHANNEL_LEG, DEFAULT_CONF_INT_VALUE);
        validateIntPropertyParameter(channelLeg, CONFIGURATION_KEY_CHANNEL_LEG);
        Servo legServo = new PCA9685Servo(device.getChannel(channelLeg));

        int channelShift = configuration.getInteger(CONFIGURATION_KEY_CHANNEL_SHIFT, DEFAULT_CONF_INT_VALUE);
        validateIntPropertyParameter(channelShift, CONFIGURATION_KEY_CHANNEL_SHIFT);
        Servo shiftServo = new PCA9685Servo(device.getChannel(channelShift));

        try {
            vehiclePlatform = new VehiclePlatform(signalEquilibrium, throttleServo, steeringServo, legServo, shiftServo);
        } catch (IOException e) {
            throw new ConfigurationException("Could not initiate device!", e);
        }
    }

	@Override
	public void onMessage(VehicleEvent message) {
        try {
            processMessage(message);
        } catch (IOException e) {
            SimpleLoggingUtil.error(getClass(), "Could process event " + message, e);

        }

    }

	private void processMessage(VehicleEvent message) throws IOException {
        switch (message.getType()){
            case THROTTLE:
                vehiclePlatform.setSteering(message.getValue());
                break;
            case STEERING:
                vehiclePlatform.setSteering(message.getValue());
                break;
            case LEG:
                break;
            case SHIFT:
                break;
        }
    }

	private void validateIntPropertyParameter(int value, String parameter) throws ConfigurationException {
		if (value == DEFAULT_CONF_INT_VALUE) {
			throw ConfigurationException.createMissingConfigNameException(parameter);
		}
	}
}
