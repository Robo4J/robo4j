/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.hw.rpi.i2c.adafruitbackpack;

import java.util.concurrent.TimeUnit;

/**
 * Simple example of using Adafruit BiColor Bargraph {@link BiColor24BarDevice}
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BiColor24BargraphExample {

	public static void main(String[] args) throws Exception {
		System.out.println("=== Bi-BiColor 24 Bargraph ===");

		BiColor24BarDevice device = new BiColor24BarDevice();
		device.clear();
		device.display();

		for (int i = 0; i < device.getMaxBar(); i++) {
			device.setBar(i, BiColor.GREEN);
			device.display();
			TimeUnit.MILLISECONDS.sleep(200);
		}

		for (int i = device.getMaxBar() - 1; i >= 0; i--) {
			device.setBar(i, BiColor.OFF);
			device.display();
			TimeUnit.MILLISECONDS.sleep(100);
		}

		int counter = 0;
		while (counter < 3) {
			for (int i = 0; i < 12; i++) {
				int colorNumber = (i + counter) % 3 + 1;
				device.setBar(i, BiColor.getByValue(colorNumber));
				TimeUnit.MILLISECONDS.sleep(200);
				device.display();
			}
			counter++;
		}

		System.out.println("Press <Enter> to quit!");
		System.in.read();
		device.clear();
		device.display();
	}

}
