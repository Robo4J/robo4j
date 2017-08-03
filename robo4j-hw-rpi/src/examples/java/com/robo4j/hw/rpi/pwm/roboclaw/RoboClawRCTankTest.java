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
package com.robo4j.hw.rpi.pwm.roboclaw;

import java.io.IOException;

import com.robo4j.hw.rpi.Servo;
import com.robo4j.hw.rpi.i2c.pwm.PCA9685Servo;
import com.robo4j.hw.rpi.i2c.pwm.PWMPCA9685Device;

/**
 * This example assumes two servos connected to specific channels (6 and 7). It
 * is important to modify this example to match your setup.
 * 
 * <b>This example should be modified to suit your setup!</b>
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboClawRCTankTest {
	// The internetz says 50Hz is the standard PWM frequency for operating RC
	// servos.
	private static final int SERVO_FREQUENCY = 50;

	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length != 3) {
			System.out.println("Usage: Tank <speed> <direction> <duration>");
			System.out.flush();
			System.exit(2);
		}
		float speed = Float.parseFloat(args[0]);
		float direction = (float) Math.toRadians(Float.parseFloat(args[1]));
		int duration = Integer.parseInt(args[2]);

		testEngine(speed, direction, duration);
		System.out.println("All done! Bye!");
	}

	public static void testEngine(float speed, float direction, int duration) throws IOException, InterruptedException {
		System.out.println(String.format("Running for %d ms with speed %f and direction %f.", duration, speed, direction));
		PWMPCA9685Device device = new PWMPCA9685Device();
		device.setPWMFrequency(SERVO_FREQUENCY);
		Servo leftEngine = new PCA9685Servo(device.getChannel(6));
		Servo rightEngine = new PCA9685Servo(device.getChannel(7));

		RoboClawRCTank tank = new RoboClawRCTank(leftEngine, rightEngine);
		tank.setDirection(direction);
		Thread.sleep(duration);
		tank.setSpeed(0);
	}
}
