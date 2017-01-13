/*
 * Copyright (C) 2017. Miroslav Wengner, Marcus Hirt
 * This GyroL3GD20Test.java  is part of robo4j.
 * module: robo4j-hw-rpi
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.hw.rpi.i2c.gyro;

import java.io.IOException;

import com.robo4j.hw.rpi.i2c.gyro.GyroL3GD20Device;
import com.robo4j.hw.rpi.i2c.gyro.GyroL3GD20Device.Sensitivity;

/**
 * Simple example which repeatedly reads the gyro. Good for checking that your
 * gyro is working.
 * 
 * @author Marcus Hirt
 */
public class GyroL3GD20Test {

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("Initializing...");
		GyroL3GD20Device device = new GyroL3GD20Device(Sensitivity.DPS_245);

		while (true) {
			System.out.println(device.read());
			Thread.sleep(200);
		}
	}
}
