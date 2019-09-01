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

package com.robo4j.hw.rpi.i2c.adafruitoled;

import com.robo4j.hw.rpi.i2c.adafruitbackpack.BiColor;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.BiColor8x8MatrixDevice;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.MatrixRotation;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.PackElement;

import java.util.concurrent.TimeUnit;

/**
 * https://learn.adafruit.com/adafruit-led-backpack/bi-color-8x8-matrix
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BiColor8x8MatrixExample {

	public static void main(String[] args) throws Exception {
		System.out.println("... BiColor 8x8 Matrix Example...");

		BiColor8x8MatrixDevice matrix = new BiColor8x8MatrixDevice();
		matrix.clear();
		matrix.display();

		for (MatrixRotation rotation : MatrixRotation.values()) {
			matrix.setRotation(rotation);
			matrix.addPixel(new PackElement(0, 0, BiColor.RED));
			matrix.addPixel(new PackElement(1, 0, BiColor.GREEN));
			matrix.addPixel(new PackElement(2, 0, BiColor.YELLOW));
			matrix.addPixel(new PackElement(3, 0, BiColor.RED));
			matrix.addPixel(new PackElement(0, 1, BiColor.GREEN));
			matrix.addPixel(new PackElement(0, 2, BiColor.YELLOW));
			matrix.addPixel(new PackElement(7, 7, BiColor.GREEN));
			matrix.addPixel(new PackElement(7, 6, BiColor.GREEN));
			matrix.display();
			TimeUnit.SECONDS.sleep(1);
			matrix.clear();
		}

		System.out.println("...Click to quit...");
		System.in.read();
		matrix.clear();
		matrix.display();

	}
}
