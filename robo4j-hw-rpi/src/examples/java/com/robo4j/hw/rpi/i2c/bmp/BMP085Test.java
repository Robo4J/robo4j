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
package com.robo4j.hw.rpi.i2c.bmp;

import java.io.IOException;

import com.robo4j.hw.rpi.i2c.bmp.BMP085Device;
import com.robo4j.hw.rpi.i2c.bmp.BMP085Device.OperatingMode;

/**
 * Repeatedly reads and displays the temperature (in C), pressure (in hPa) and
 * barometric altitude (in m). Good example to test that your BMP device is working.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BMP085Test {
	public static void main(String[] args) throws IOException, InterruptedException {
		BMP085Device bmp = new BMP085Device(OperatingMode.STANDARD);
		while (true) {
			System.out.printf("Temperature: %sC, Pressure: %shPa, Altitude: %sm%n",
					bmp.readTemperature(),
					bmp.readPressure() / 100,
					bmp.readAltitude());
			Thread.sleep(2000);
		}
	}
}
