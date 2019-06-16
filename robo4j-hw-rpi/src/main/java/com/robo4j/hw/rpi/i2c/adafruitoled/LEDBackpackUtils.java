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

/**
 * Useful support utils to process Arduino demos
 *
 * https://learn.adafruit.com/adafruit-led-backpack/bi-color-8x8-matrix
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class LEDBackpackUtils {

	/**
	 *  create a BiColor byte array for given matrix size
	 *
	 *
 	 * @param sequence sequence of BiColor values
	 * @return byte array
	 */
    public static byte[] convertBiColorMatrixCharSequenceToArray(int size, char delimiter, char... sequence){
        byte[] result = new byte[size * size];
        int pos = 0;
        for(char c: sequence){
        	if(c != delimiter){
				result[pos++] = (byte)Character.getNumericValue(c);
			}
        }
        return result;
    }

	/**
	 * based on byte array creates PackElements
	 *
	 * @param matrixSize
	 *            matrix size
	 * @param array
	 *            BiColor array values of the matrix, matrix started from position 0
	 * @return PackElements array
	 */
	public static PackElement[] createByArray(int matrixSize, byte[] array) {

		PackElement[] result = new PackElement[matrixSize * matrixSize];
		int y = 0;
		PackElement firstElement = new PackElement(0, 0, BiColor.getByValue(array[0]));
		result[0] = firstElement;
		for (int i = 1; i < array.length; i++) {
			if (i % matrixSize == 0) {
				y++;
			}
			int x = i % matrixSize;
			BiColor color = BiColor.getByValue(array[i]);
			result[i] = new PackElement(x, y, color);
		}
		return result;
	}

}
