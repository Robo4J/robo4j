/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
import java.io.InputStream;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;

/**
 * Small example panning and tilting two servos in a pan/tilt setup.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ServoUnitExample {
	private static final int PAN_STEPS = 30;
	private static final int TILT_STEPS = 10;

	private static volatile boolean stop = false;

	public static void main(String[] args) throws RoboBuilderException {
		RoboBuilder builder = new RoboBuilder();
		InputStream settings = ServoUnitExample.class.getClassLoader().getResourceAsStream("servoexample.xml");
		if (settings == null) {
			System.out.println("Could not find the settings for the ServoUnitExample!");
			System.exit(2);
		}
		builder.add(settings);
		RoboContext ctx = builder.build();

		RoboReference<Float> panRef = ctx.getReference("pan");
		RoboReference<Float> tiltRef = ctx.getReference("tilt");

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				float panDirection = 1.0f;
				while (!stop) {
					for (int tiltStep = 0; tiltStep < TILT_STEPS; tiltStep++) {
						// Just move the tilt a quarter of max positive.
						float tilt = tiltStep / (TILT_STEPS * 4.0f);
						tiltRef.sendMessage(tilt);
						for (int panStep = 0; panStep < PAN_STEPS; panStep++) {
							if (stop) {
								break;
							}
							float pan = (panStep * 2.0f / PAN_STEPS - 1.0f) * panDirection;
							panRef.sendMessage(pan);
							sleep(50);
						}
						panDirection *= -1;
					}
				}
			}
		});
		thread.setDaemon(true);
		thread.start();

		System.out.println("Press <Enter> to quit!");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		stop = true;
		ctx.shutdown();
	}

	private static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
