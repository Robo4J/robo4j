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
package com.robo4j.units.rpi.accelerometer;

import java.io.IOException;
import java.io.InputStream;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.util.SystemUtil;

/**
 * Runs the accelerometer continuously and always prints what it reads.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AccelerometerExample {
	private static final String ID_PROCESSOR = "processor";

	public static void main(String[] args) throws RoboBuilderException, IOException {
		RoboBuilder builder = new RoboBuilder();
		InputStream settings = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("accelerometerexample.xml");
		if (settings == null) {
			System.out.println("Could not find the settings for the GyroExample!");
			System.exit(2);
		}
		builder.add(settings);
		builder.add(AccelerometerProcessor.class, ID_PROCESSOR);
		RoboContext ctx = builder.build();

		System.out.println("State before start:");
		System.out.println(SystemUtil.printStateReport(ctx));
		ctx.start();

		System.out.println("State after start:");
		System.out.println(SystemUtil.printStateReport(ctx));

		RoboReference<AccelerometerRequest> accelerometer = ctx.getReference("accelerometer");
		RoboReference<AccelerometerEvent> processor = ctx.getReference(ID_PROCESSOR);

		System.out.println("Press enter to start!");
		System.in.read();
		accelerometer.sendMessage(new AccelerometerRequest(processor, true, (Float3D) -> true));
		System.out.println("Will report angular changes indefinitely.\nPress enter to quit!");
		System.in.read();
	}
}
