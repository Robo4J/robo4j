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

package com.robo4j.hw.rpi.pwm;

import com.robo4j.hw.rpi.Servo;
import com.robo4j.hw.rpi.i2c.pwm.PCA9685Servo;
import com.robo4j.hw.rpi.i2c.pwm.PWMPCA9685Device;

import java.io.IOException;

/**
 * This example assumes servo is connected to the specific channel
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class PCA9685TruckPlatformExample {

	private static final int SERVO_THROTTLE = 0;
	private static final int SERVO_STEERING = 5;
	private static final int SERVO_LEG = 6;
	private static final int SERVO_SHIFT = 7;
	private static final int SERVO_FREQUENCY = 250;

	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length != 5) {
			System.out.println(String.format("Usage: %s <throttle> <steering> <leg> <shift> <duration>",
					PCA9685TruckPlatformExample.class.getSimpleName()));
			System.out.flush();
			System.exit(2);
		}
		float throttle = Float.parseFloat(args[0]);
		float steering = Float.parseFloat(args[1]);
		float leg = Float.parseFloat(args[2]);
		float shift = Float.parseFloat(args[3]);
		int duration = Integer.parseInt(args[4]);

		testMotor(throttle, steering, leg, shift, duration);
		System.out.println("All done! Bye!");
	}

	public static void testMotor(float throttle, float steering, float leg, float shift, int duration)
			throws IOException, InterruptedException {
		System.out.println(String.format("Running for %d ms with throttle %f, steering %f, leg %f, shift %f", duration,
				throttle, steering, leg, shift));
		PWMPCA9685Device device = new PWMPCA9685Device();
		device.setPWMFrequency(SERVO_FREQUENCY);
		Servo throttleEngine = new PCA9685Servo(device.getChannel(SERVO_THROTTLE));
		Servo steeringEngine = new PCA9685Servo(device.getChannel(SERVO_STEERING));
		Servo legEngine = new PCA9685Servo(device.getChannel(SERVO_LEG));
		Servo shiftEngine = new PCA9685Servo(device.getChannel(SERVO_SHIFT));

		VehiclePlatform vehicle = new VehiclePlatform(0, throttleEngine, steeringEngine, legEngine, shiftEngine);
		vehicle.setThrottle(throttle);
		vehicle.setSteering(steering);
		vehicle.setLeg(leg);
		vehicle.setShift(shift);
		Thread.sleep(duration);
		vehicle.setThrottle(0);
		vehicle.setSteering(0);
		vehicle.setLeg(0);
		vehicle.setShift(0);
	}

}
