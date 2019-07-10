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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BiColor8x8MatrixFaceRotationExample {

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("=== BiColor 8x8 Matrix Face Rotation Example ===");

		BiColor8x8MatrixDevice matrix = new BiColor8x8MatrixDevice();
		//@formatter:off
        byte[] faceSmile = {
                  0b0011_1100,
                  0b0100_0010,
            (byte)0b1010_0101,
            (byte)0b1000_0001,
            (byte)0b1010_0101,
            (byte)0b1001_1001,
            (byte)0b0100_0010,
            (byte)0b0011_1100};
        //@formatter:on

		int color = 1;
		for (int i = 0; i < faceSmile.length; i++) {
			matrix.clear();
			matrix.display();
			matrix.addPixels(
					LEDBackpackUtils.create2DMatrixByRowArraysAndColor(faceSmile, BiColor.getByValue(i % 2 + 1)));
			matrix.display();
			matrix.setRotation(MatrixRotation.getById(i % 5 + 1));
			TimeUnit.SECONDS.sleep(1);
		}

		System.out.println("Press <Enter> to quit!");
		System.in.read();
		matrix.clear();
		matrix.display();
	}
}
