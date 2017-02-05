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

/**
 * Sanity check every 500ms to see that data is being retrieved.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class MagnetometerLSM303Test {
	// FIXME(Marcus/Dec 5, 2016): Verify that this one works.
	public static void main(String[] args) throws IOException, InterruptedException {
		MagnetometerLSM303Device device = new MagnetometerLSM303Device();

		Float3D min = new Float3D();
		min.x = Float.MAX_VALUE;
		min.y = Float.MAX_VALUE;
		min.z = Float.MAX_VALUE;

		Float3D max = new Float3D();
		max.x = Float.MIN_VALUE;
		max.y = Float.MIN_VALUE;
		max.z = Float.MIN_VALUE;

		while (true) {
			Float3D fl = device.read();
			if (updateMin(min, fl)) {
				System.out.println("Min: " + fl);
			}
			if (updateMax(max, fl)) {
				System.out.println("Max: " + fl);
			}
			Thread.sleep(500);
		}
	}


	//Private Methods
	//TODO:: maybe duplicate functionality
	private static boolean updateMin(Float3D min, Float3D newVal) {
		boolean isUpdated = false;

		if (newVal.x < min.x) {
			min.x = newVal.x;
			isUpdated = true;
		}
		if (newVal.y < min.y) {
			min.y = newVal.y;
			isUpdated = true;
		}
		if (newVal.z < min.z) {
			min.z = newVal.z;
			isUpdated = true;
		}
		return isUpdated;
	}

	private static boolean updateMax(Float3D max, Float3D newVal) {
		boolean isUpdated = false;

		if (newVal.x > max.x) {
			max.x = newVal.x;
			isUpdated = true;
		}
		if (newVal.y > max.y) {
			max.y = newVal.y;
			isUpdated = true;
		}
		if (newVal.z > max.z) {
			max.z = newVal.z;
			isUpdated = true;
		}
		return isUpdated;
	}

}
