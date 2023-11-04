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
package com.robo4j.hw.rpi.i2c.lidar;

import java.io.IOException;

import com.robo4j.hw.rpi.i2c.lidar.LidarLiteDevice;

/**
 * This example will repeatedly acquire the range with the LidarLite. Good for
 * testing that it works.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class LidarLiteTest {
	public static void main(String[] args) throws IOException, InterruptedException {
		LidarLiteDevice ld = new LidarLiteDevice();
		while (true) {
			ld.acquireRange();
			Thread.sleep(100);
			System.out.println(String.format("Distance: %.02fm", ld.readDistance()));
			Thread.sleep(500);
		}
	}

}
