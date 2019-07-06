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

package com.robo4j.hw.rpi.imu.bno;

/**
 * ShtpUtils collection of useful utils
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ShtpUtils {

	/**
	 * Given a register value and a Q point, convert to float See
	 * https://en.wikipedia.org/wiki/Q_(number_format)
	 *
	 * @param fixedPointValue
	 *            fixed point value
	 * @param qPoint
	 *            q point
	 * @return float value
	 */
	public static float intToFloat(int fixedPointValue, int qPoint) {
		float qFloat = fixedPointValue & 0xFFFF;
		qFloat *= Math.pow(2, (qPoint & 0xFF) * -1);
		return qFloat;
	}

	/**
	 * Print hexadecimal values of int array to system output
	 * 
	 * @param message
	 *            message
	 * @param array
	 *            array int values
	 */
	public static void printArray(String message, int[] array) {
		System.out.print("printArray: " + message);
		for (int i = 0; i < array.length; i++) {
			System.out.print(" " + Integer.toHexString(array[i] & 0xFF) + ",");
		}
		System.out.print("\n");
	}
}
