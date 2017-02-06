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
package com.robo4j.units.rpi.pwm;

import java.io.IOException;

import com.robo4j.core.RoboBuilder;
import com.robo4j.core.RoboBuilderException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;

public class ServoUnitExample {
	private final static int STEPS = 30;
	private static volatile boolean stop = false;

	public static void main(String[] args) throws RoboBuilderException {
		RoboBuilder builder = new RoboBuilder();
		builder.add(ServoUnitExample.class.getClassLoader().getResourceAsStream("robo4j.xml"));
		RoboContext ctx = builder.build();

		RoboReference<Float> panRef = ctx.getReference("pan");
		RoboReference<Float> tiltRef = ctx.getReference("tilt");

		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("Press enter to quit!");
				try {
					System.in.read();
				} catch (IOException e) {
					e.printStackTrace();
				}
				stop = true;
			}
		}).start();

		float panDirection = 1.0f;
		while (!stop) {
			for (int tiltStep = 0; tiltStep < STEPS; tiltStep++) {
				// Just move the tilt a quarter of max positive.
				float tilt = tiltStep / (STEPS * 25);
				tiltRef.sendMessage(tilt);
				for (int panStep = 0; panStep < STEPS; panStep++) {
					float pan = (panStep * 2.0f / STEPS - 1.0f) * panDirection;
					panRef.sendMessage(pan);
					sleep(100);
				}
			}
			panDirection *= -1;
		}
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
