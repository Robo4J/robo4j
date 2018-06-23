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
import com.robo4j.units.rpi.I2CEndPoint;
import com.robo4j.units.rpi.I2CRegistry;
import com.robo4j.units.rpi.I2CRoboUnit;
import com.robo4j.units.rpi.roboclaw.MotionEvent;

/**
 * VehiclePlatformUnit is the example which control the vehicle platform with
 * two motors: * 1. throttle motor * 2. rotate servo
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class VehiclePlatformUnit extends I2CRoboUnit<MotionEvent> {

    /**
     * SERVO values corresponds to the channel used on PWM PC9685
     */
    private static final int SERVO_THROTTLE = 0;
    private static final int SERVO_FREQUENCY = 250;
    private static final int SERVO_STEERING = 5;
    private static final int SERVO_LEG = 6;
    private static final int SERVO_SHIFT = 7;
	public static String CONFIGURATION_KEY_ROTATE_CHANNEL = "rotateChannel";
	public static String CONFIGURATION_KEY_ROTATE_INVERTED = "rotateInverted";
	public static String CONFIGURATION_KEY_THROTTLE_CHANNEL = "throttleChannel";
	public static String CONFIGURATION_KEY_THROTTLE_INVERTED = "throttleInverted";

	private VehiclePlatform vehiclePlatform;

	public VehiclePlatformUnit(RoboContext context, String id) {
		super(MotionEvent.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
        super.onInitialization(configuration);
        Object pwmDevice = I2CRegistry.getI2CDeviceByEndPoint(new I2CEndPoint(getBus(), getAddress()));
        PWMPCA9685Device device = PCA9685Utils.initPwmDevice(pwmDevice, getBus(), getAddress(), SERVO_FREQUENCY);
        Servo throttleEngine = new PCA9685Servo(device.getChannel(SERVO_THROTTLE));
        Servo steeringEngine = new PCA9685Servo(device.getChannel(SERVO_STEERING));
        Servo legEngine = new PCA9685Servo(device.getChannel(SERVO_LEG));
        Servo shiftEngine = new PCA9685Servo(device.getChannel(SERVO_SHIFT));
	}

}
