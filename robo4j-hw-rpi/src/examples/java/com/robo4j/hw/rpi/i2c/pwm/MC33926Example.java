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
package com.robo4j.hw.rpi.i2c.pwm;

import java.io.IOException;

import com.pi4j.io.gpio.RaspiPin;

/**
 * This example assumes an MC33926 hooked up to channel 4 of .
 * 
 * ___VALIDATE THAT YOUR SETUP MATCHES THIS, OR MODIFY THE EXAMPLE!___
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class MC33926Example {

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("Creating device...");
		
		if (args.length != 3) {
			System.out.println("Usage: MC33926Example <speed> <duration>");
			System.out.flush();
			System.exit(2);
		}
		float speed = Float.parseFloat(args[0]);
		int duration = Integer.parseInt(args[2]);

		runEngine(speed, duration);
	}

	private static void runEngine(float speed, int duration) throws IOException, InterruptedException {
		PWMPCA9685Device pwm = new PWMPCA9685Device();
		PololuMC33926HBridgeEngine engine = new PololuMC33926HBridgeEngine("Engine", pwm.getChannel(4), RaspiPin.GPIO_02, RaspiPin.GPIO_03, true);

		System.out.println(String.format("Running for %d ms at speed %f...", duration, speed));

		engine.setSpeed(speed);
		Thread.sleep(duration);
		engine.setSpeed(0);
		
		System.out.println("Done!");
	}
}
