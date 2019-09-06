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
 * Simple example using {@link BiColor8x8MatrixDevice}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BiColor8x8MatrixExample {

	public static void main(String[] args) throws Exception {
		System.out.println("=== BiColor 8x8 Matrix Example ===");

		BiColor8x8MatrixDevice matrix = new BiColor8x8MatrixDevice();
		matrix.clear();
		matrix.display();

		MatrixRotation[] rotations = { MatrixRotation.DEFAULT_X_Y, MatrixRotation.RIGHT_90, MatrixRotation.RIGHT_180,
				MatrixRotation.RIGHT_270, MatrixRotation.LEFT_90 };
		for (MatrixRotation rotation : rotations) {
			matrix.setRotation(rotation);
			matrix.drawPixel(0, 0, BiColor.RED);
			matrix.drawPixel(1, 0, BiColor.GREEN);
			matrix.drawPixel(2, 0, BiColor.YELLOW);
			matrix.drawPixel(3, 0, BiColor.RED);
			matrix.drawPixel(0, 1, BiColor.GREEN);
			matrix.drawPixel(0, 2, BiColor.YELLOW);
			matrix.drawPixel(7, 7, BiColor.YELLOW);
			matrix.drawPixel(7, 6, BiColor.YELLOW);
			matrix.display();
			TimeUnit.SECONDS.sleep(1);
			matrix.clear();
		}

		System.out.println("Press <Enter> to quit!");
		System.in.read();
		matrix.clear();
		matrix.display();

	}
}
