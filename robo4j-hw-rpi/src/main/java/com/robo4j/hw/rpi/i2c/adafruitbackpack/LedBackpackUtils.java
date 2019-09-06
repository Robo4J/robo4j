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

/**
 * Useful support utils to process Arduino demos
 *
 * https://learn.adafruit.com/adafruit-led-backpack/bi-color-8x8-matrix
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class LedBackpackUtils {

	/**
	 * Create a BiColor byte array for given matrix size example: sequence =
	 * "00123300,03000030,30200301,30000003,30300303,30033003,03000030,00333300"
	 * where number are according to {@link BiColor}
	 *
	 * @param sequence
	 *            sequence of BiColor values
	 * @return byte array
	 */
	public static byte[] createMatrixBiColorArrayByCharSequence(int size, char delimiter, char... sequence) {
		byte[] result = new byte[size * size];
		int pos = 0;
		for (char c : sequence) {
			if (c != delimiter) {
				result[pos++] = (byte) Character.getNumericValue(c);
			}
		}
		return result;
	}

	/**
	 * Paint the byte array by defining array of bytes array and selected color.
	 * 
	 * Example: byte[] face_smile = { 0b0011_1100, 0b0100_0010,
	 * (byte)0b1010_0101, (byte)0b1000_0001, (byte)0b1010_0101,
	 * (byte)0b1001_1001, (byte)0b0100_0010, (byte)0b0011_1100};
	 *
	 * @param ledDevice
	 *            the LED matrix to paint on.
	 * @param array
	 *            unsigned byte array represented by 8-bits, represent the
	 *            matrix of size array.length.
	 * @param color
	 *            the desired color.
	 */
	public static void paintToByRowArraysAndColor(MatrixLedDevice ledDevice, byte[] array, BiColor color) {
		int size = array.length * array.length;

		for (int i = 0; i < size; i++) {

			int x = i % array.length;
			int y = i / array.length;

			byte shift = (byte) (array[y] >> (array.length - x - 1));
			boolean on = (shift & 0x01) == 1;
			ledDevice.drawPixel(x, y, on ? color : BiColor.OFF);
		}
	}

	/**
	 * Paints the information in the byte array on the provide LED matrix
	 * device.
	 *
	 * @param ledDevice
	 *            the LED matrix to paint on.
	 * @param array
	 *            BiColor array values of the matrix, matrix started from
	 *            position 0
	 */
	public static void paintByBiColorByteArray(MatrixLedDevice ledDevice, byte[] array) {
		int y = 0;
		int matrixWidth = ledDevice.getWidth();
		for (int i = 1; i < array.length; i++) {
			if (i % matrixWidth == 0) {
				y++;
			}
			int x = i % matrixWidth;
			BiColor color = BiColor.getByValue(array[i]);
			ledDevice.drawPixel(x, y, color);
		}
	}
}
