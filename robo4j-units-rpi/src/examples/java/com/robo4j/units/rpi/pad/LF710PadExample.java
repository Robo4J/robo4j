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
package com.robo4j.units.rpi.pad;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.util.SystemUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Logitech F710 Pad Example
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class LF710PadExample {

	public static void main(String[] args) throws RoboBuilderException, IOException {
		InputStream settings = Thread.currentThread().getContextClassLoader().getResourceAsStream("logitechF710.xml");
		if (settings == null) {
			System.out.println("Could not find the settings for the Gamepad!");
			System.exit(2);
		}
		RoboBuilder builder = new RoboBuilder();
		builder.add(settings);
		RoboContext sytem = builder.build();

		System.out.println("... Gamepad buttons Example ...");
		sytem.start();

		System.out.println(SystemUtil.printStateReport(sytem));

		System.out.println("Press <Enter> to quit!");
		System.in.read();
		sytem.shutdown();
		System.out.println("Bye!");
	}
}
