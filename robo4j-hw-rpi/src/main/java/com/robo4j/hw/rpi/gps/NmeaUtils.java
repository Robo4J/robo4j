/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 * 
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.hw.rpi.gps;

/**
 * Utilities to help with Nmea messages.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class NmeaUtils {

	/**
	 * Checks the NEMA line checksum.
	 * 
	 * @param data
	 *            a line of NEMA data.
	 * @return true if the checksum is valid.
	 */
	public final static boolean hasValidCheckSum(String data) {
		if (!data.startsWith("$")) {
			return false;
		}
		try {
			int indexOfStar = data.indexOf('*');
			if (indexOfStar <= 0 || indexOfStar >= data.length()) {
				return false;
			}
			String chk = data.substring(1, indexOfStar);
			String checksumStr = data.substring(indexOfStar + 1);
			int valid = Integer.parseInt(checksumStr.trim(), 16);
			int checksum = 0;
			for (int i = 0; i < chk.length(); i++) {
				checksum = checksum ^ chk.charAt(i);
			}
			return checksum == valid;
		} catch (Exception e) {
			return false;
		}
	}

	public static int getInt(String string) {
		if (string == null || "".equals(string)) {
			return -1;
		}
		return Integer.parseInt(string);
	}

	public static float getFloat(String string) {
		if (string == null || "".equals(string)) {
			return Float.NaN;
		}
		return Float.parseFloat(string);
	}

	public static float parseNmeaFormatCoordinate(String string) {
		int index = string.indexOf('.');
		if (index < 0) {
			return Float.NaN;
		}
		float minutes = Float.parseFloat(string.substring(index - 2));
		float degrees = Integer.parseInt(string.substring(0, index - 2));
		return degrees + minutes / 60.0f;
	}

	public static String cleanLine(String dataLine) {
		// We only attempt to recover the first part, and only if it looks
		// like it could be an ok string.
		if (dataLine.startsWith("$")) {
			return dataLine.substring(0, Math.min(dataLine.indexOf('*') + 3, dataLine.length()));
		}
		return dataLine;
	}
}
