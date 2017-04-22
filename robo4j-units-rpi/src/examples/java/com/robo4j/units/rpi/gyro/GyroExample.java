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
package com.robo4j.units.rpi.gyro;

import java.io.IOException;
import java.io.InputStream;

import com.robo4j.core.RoboBuilder;
import com.robo4j.core.RoboBuilderException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.math.geometry.Float3D;

/**
 * Runs the laser scanner, printing the max range and min range found on stdout.
 * (To see all data, run with JFR and dump a recording.)
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class GyroExample {
	private static final String ID_PROCESSOR = "processor";

	public static void main(String[] args) throws RoboBuilderException, IOException {
		RoboBuilder builder = new RoboBuilder();
		InputStream settings = GyroExample.class.getClassLoader().getResourceAsStream("gyroexample.xml");
		if (settings == null) {
			System.out.println("Could not find the settings for the GyroExample!");
			System.exit(2);
		}
		builder.add(GyroProcessor.class, ID_PROCESSOR);
		RoboContext ctx = builder.build();
		RoboReference<GyroRequest> gyro = ctx.getReference("gyro");
		RoboReference<GyroEvent> processor = ctx.getReference(ID_PROCESSOR);
		
		System.out.println("Let the gyro unit be absolutely still, then press enter to calibrate and start!");
		System.in.read();
		gyro.sendMessage(new GyroRequest(processor, true, true, new Float3D(1.0f, 1.0f, 1.0f)));
		System.out.println("Will report angular changes indefinitely.\nPress enter to quit!");
		System.in.read();
	}
}
