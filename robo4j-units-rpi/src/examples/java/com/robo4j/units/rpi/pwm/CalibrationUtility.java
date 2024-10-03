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
package com.robo4j.units.rpi.pwm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.util.SystemUtil;

/**
 * Small calibration utility to help fine tune a servo.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CalibrationUtility {
	public static void main(String[] args) throws RoboBuilderException, FileNotFoundException {
		InputStream settings = ServoUnitExample.class.getClassLoader().getResourceAsStream("calibration.xml");
		if (args.length != 1) {
			System.out.println("No file specified, using default calibration.xml");
		} else {
			settings = new FileInputStream(args[0]);
		}

		RoboBuilder builder = new RoboBuilder();
		if (settings == null) {
			System.out.println("Could not find the settings for servo calibration test!");
			System.exit(2);
		}
		builder.add(settings);
		RoboContext ctx = builder.build();
		System.out.println("State before start:");
		System.out.println(SystemUtil.printStateReport(ctx));
		ctx.start();

		System.out.println("State after start:");
		System.out.println(SystemUtil.printStateReport(ctx));

		String lastCommand;
		Scanner scanner = new Scanner(System.in);
		System.out.println(
				"Type the servo to control and how much to move the servo, between -1 and 1. For example:\npan -1.0\nType q and enter to quit!\n");
		while (!"q".equals(lastCommand = scanner.nextLine())) {
			lastCommand = lastCommand.trim();
			String[] split = lastCommand.split(" ");
			if (split.length != 2) {
				System.out.println("Could not parse " + lastCommand + ". Please try again!");
				continue;
			}
			RoboReference<Float> servoRef = ctx.getReference(split[0]);
			if (servoRef == null) {
				System.out.println("Could not find any robo unit named " + split[0] + ". Please try again!");
				continue;
			}
			try {
				float value = Float.parseFloat(split[1]);
				servoRef.sendMessage(value);
			} catch (Exception e) {
				System.out.println(
						"Could not parse " + split[1] + " as a float number. Error message was: " + e.getMessage() + ". Please try again!");
				continue;
			}
		}
		ctx.shutdown();
		scanner.close();
	}
}