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
package com.robo4j.hw.rpi.i2c.magnetometer;

import java.io.IOException;

import com.robo4j.math.geometry.Float3D;
import com.robo4j.math.geometry.Int3D;

/**
 * Sanity check every 500ms to see that data is being retrieved.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class MagnetometerLSM303Test {
	// FIXME(Marcus/Dec 5, 2016): Verify that this one works.
	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length < 2) {
			System.out.println("Usage: MagnetometerLSM303Test <delay between reads (ms)> <print every Nth read> [<print raw (true|false)>] ");	
			System.exit(1);
		}
		int delay = Integer.parseInt(args[0]);
		int modulo = Integer.parseInt(args[1]);
		boolean printRaw = false;
		if (args.length >= 3) {
			printRaw = Boolean.parseBoolean(args[2]);
		}
		MagnetometerLSM303Device device = new MagnetometerLSM303Device();
		int count = 0;
		while (true) {
			if (count % modulo == 0) {
				if (printRaw) {
					Int3D fl = device.readRaw();
					System.out.println(String.format("Raw Value %d = %s\tHeading:%000.0f", count, fl.toString(), MagnetometerLSM303Device.getCompassHeading(fl)));					
				} else {
					Float3D fl = device.read();
					System.out.println(String.format("Value %d = %s", count, fl.toString()));
				}
			}
			count++;
			Thread.sleep(delay);
		}
	}
}
