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

import com.robo4j.hw.rpi.i2c.adafruitbackpack.BiColor8x8MatrixDevice;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.LEDBackpackUtils;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.PackElement;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BiColor8x8MatrixFaceExample {

	public static void main(String[] args) throws Exception {
		System.out.println("... BiColor 8x8 Matrix Example...");

		BiColor8x8MatrixDevice matrix = new BiColor8x8MatrixDevice();

		char[] face_smile = "00333300,03000030,30300303,30000003,30300303,30033003,03000030,00333300".toCharArray();
		char[] face_neutral = "00222200,02000020,20200202,20000002,20222202,20000002,02000020,00222200".toCharArray();
		char[] face_frown = "00111100,01000010,10100101,10000001,10011001,10100101,01000010,00111100".toCharArray();

		List<char[]> availableFaces = Arrays.asList(face_frown, face_neutral, face_smile);

		for (char[] face : availableFaces) {
			matrix.clear();
			matrix.display();
			byte[] faceByte = LEDBackpackUtils.createMatrixBiColorArrayByCharSequence(matrix.getMatrixSize(), ',',
					face);
			PackElement[] faceElements = LEDBackpackUtils.createMatrixByBiColorByteArray(matrix.getMatrixSize(), faceByte);
			matrix.addPixels(faceElements);
			matrix.display();
			TimeUnit.SECONDS.sleep(1);
		}

		System.out.println("...Click to quit...");
		System.in.read();
		matrix.clear();
		matrix.display();

	}
}
