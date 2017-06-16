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

import com.robo4j.math.geometry.Tuple3f;
import com.robo4j.math.geometry.Tuple3i;

/**
 * Example program that can be used to produce csv data that can be used for calibration.
 * 
 * <p>
 * Example: MagnetometerLSM303Test 100 1 csv
 * </p>
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class MagnetometerLSM303Test {
	private enum PrintStyle {
		PRETTY, RAW, CSV
	}

	// FIXME(Marcus/Dec 5, 2016): Verify that this one works.
	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length < 2) {
			System.out.println(
					"Usage: MagnetometerLSM303Test <delay between reads (ms)> <print every Nth read> [<print style (pretty|raw|csv)>] ");
			System.exit(1);
		}
		int delay = Integer.parseInt(args[0]);
		int modulo = Integer.parseInt(args[1]);
		PrintStyle printStyle = PrintStyle.PRETTY;
		if (args.length >= 3) {
			printStyle = PrintStyle.valueOf(args[2].toUpperCase());
		}
		MagnetometerLSM303Device device = new MagnetometerLSM303Device();
		int count = 0;
		while (true) {
			Tuple3i fl;
			if (count % modulo == 0) {
				switch (printStyle) {
				case RAW:
					fl = device.readRaw();
					System.out.println(String.format("Raw Value %d = %s\tHeading:%000.0f", count, fl.toString(),
							MagnetometerLSM303Device.getCompassHeading(fl)));
					break;
				case CSV:
					fl = device.readRaw();
					System.out.println(String.format("%d;%d;%d", fl.x, fl.y, fl.z));
					break;
				default:
					Tuple3f val = device.read();
					System.out.println(String.format("Value %d = %s", count, val.toString()));
				}
				count++;
				Thread.sleep(delay);
			}
		}
	}
}
