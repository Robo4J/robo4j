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
package com.robo4j.hw.rpi.i2c.pwm;

import java.io.IOException;

import com.robo4j.hw.rpi.i2c.pwm.PWMPCA9685Device;
import com.robo4j.hw.rpi.Servo;
import com.robo4j.hw.rpi.i2c.pwm.PCA9685Servo;
import com.robo4j.hw.rpi.i2c.pwm.PWMPCA9685Device.PWMChannel;

/**
 * This example assumes two servos connected to channel 0 and 1, and two H bridges controlling DC engines on channel 2 and 3. 
 * <b>This example should be modified to suit your setup!</b>
 * 
 * ___DO NOT RUN THIS EXAMPLE WITH SERVOS ON CHANNEL 2 and 3!___ 
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class PCA9685Example {
	// The internetz says 50Hz is the standard PWM frequency for operating RC servos.  
	private static final int SERVO_FREQUENCY = 50;
	private static final int MOTOR_MIN = 0;
	private static final int MOTOR_MEDIUM = 2048;
	private static final int MOTOR_MAX = 4095;

	public static void main(String[] args) throws IOException,
			InterruptedException {
		System.out.println("Creating device...");
		PWMPCA9685Device device = new PWMPCA9685Device();
		device.setPWMFrequency(SERVO_FREQUENCY);
		Servo servo0 = new PCA9685Servo(device.getChannel(0));
		Servo servo1 = new PCA9685Servo(device.getChannel(1));
		PWMChannel motor0 = device.getChannel(2);
		PWMChannel motor1 = device.getChannel(3);
		
		System.out.println("Setting start conditions...");
		servo0.setInput(0);
		servo1.setInput(0);
		motor0.setPWM(0, MOTOR_MIN);
		motor1.setPWM(0, MOTOR_MIN);

		System.out.println("Press <Enter> to run loop!");
		System.in.read();
		System.out.println("Running perpetual loop...");
		while (true) {
			servo0.setInput(-1);
			servo1.setInput(-1);
			motor0.setPWM(0, MOTOR_MEDIUM);
			motor1.setPWM(0, MOTOR_MEDIUM);
			Thread.sleep(500);
			servo0.setInput(1);;
			servo1.setInput(1);;
			motor0.setPWM(0, MOTOR_MAX);
			motor1.setPWM(0, MOTOR_MAX);
			Thread.sleep(500);
			servo0.setInput(0);
			servo1.setInput(0);
			motor0.setPWM(0, MOTOR_MIN);
			motor1.setPWM(0, MOTOR_MIN);
			Thread.sleep(1000);
		}
	}
}
