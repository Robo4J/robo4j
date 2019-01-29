/*
 * Copyright (c) 2014-2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.hw.rpi.i2c.gyro;

import java.io.IOException;

import com.robo4j.hw.rpi.i2c.gyro.GyroL3GD20Device;
import com.robo4j.hw.rpi.i2c.gyro.GyroL3GD20Device.Sensitivity;

/**
 * Simple example which repeatedly reads the gyro. Good for checking that your
 * gyro is working.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CalibratedGyroTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("Initializing...");
		GyroL3GD20Device device = new GyroL3GD20Device(Sensitivity.DPS_245);
		CalibratedGyro gyro = new CalibratedGyro(device);
		
		System.out.println("Keep the device still, and press enter to start calibration.");
		System.in.read();
		System.out.println("Calibrating...");
		gyro.calibrate();
		System.out.println("Calibration done!");
		
		while (true) {
			System.out.println(gyro.read());
			Thread.sleep(200);
		}
	}
}
