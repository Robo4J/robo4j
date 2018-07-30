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

package com.robo4j.units.lego;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.hw.lego.util.EscapeButtonUtil;
import com.robo4j.util.SystemUtil;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class InfraSensorExample {

	public static void main(String[] args) throws Exception {
		final String robo4jConfig = "robo4jInfraExample.xml";
		InputStream settings = InfraSensorExample.class.getClassLoader().getResourceAsStream(robo4jConfig);
		if (args.length != 1) {
			System.out.println(String.format("No file specified, using default %s", robo4jConfig));
		} else {
			settings = new FileInputStream(args[0]);
		}

		final RoboBuilder builder = new RoboBuilder();
		if (settings == null) {
			System.out.println("Could not find the settings for test!");
			System.exit(2);
		}

		builder.add(settings);
		RoboContext system = builder.build();
		System.out.println("State before start:");
		System.out.println(SystemUtil.printStateReport(system));

		system.start();

		RoboReference<String> lcd = system.getReference("lcd");
		lcd.sendMessage("Robo4J.io");

		RoboReference<String> infraSensor = system.getReference("infraSensor");
		infraSensor.sendMessage("start");

		shutdown(system);
	}

	private static void shutdown(RoboContext system) {
		EscapeButtonUtil.waitForPressAndRelease();
		system.shutdown();
	}
}
