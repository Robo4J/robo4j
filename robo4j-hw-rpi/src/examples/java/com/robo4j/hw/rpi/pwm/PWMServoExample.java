/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.hw.rpi.utils.GpioPin;

import java.io.IOException;

/**
 * This example assumes a servo connected to the GPIO_01 hardware PWM pin.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class PWMServoExample {
	public static void main(String[] args) throws IOException,
			InterruptedException {
		if (args.length != 1) {
			System.out.println("Usage: PWMExample <input>");
			System.exit(2);
		}
		float input = Float.parseFloat(args[0]);
		PWMServo servo = new PWMServo(GpioPin.GPIO_01, false);
		System.out.println("Setting input to " + input);
		servo.setInput(input);
		System.out.println("Press <Enter> to quit");
		System.in.read();
	}
}
