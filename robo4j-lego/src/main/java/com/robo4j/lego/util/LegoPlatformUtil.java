/*
 * Copyright (C)  2016. Miroslav Wengner and Marcus Hirt
 * This LegoPlatformUtil.java  is part of robo4j.
 *
 *  robo4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  robo4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.lego.util;

/**
 * Utils helps to adjust command angle, movement
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 09.07.2016
 */
public final class LegoPlatformUtil {
	private static final int CENTIMETER_CYCLES = 33;
	private static final int ROTATION_CYCLES = 6;
	private static final int DEFAULT_VALUE = 0;

	private LegoPlatformUtil() {
		// no instance
	}

	/**
	 * Current adjustment for Angle and Distance
	 *
	 * @param value
	 *            - value comes from command
	 * @return - number of cycles
	 */
	public static int adjustPlatformCyclesByValue(boolean angle, String value) {
		final int number = Integer.valueOf(value);
		return angle ? ROTATION_CYCLES * number : CENTIMETER_CYCLES * number;
	}

	public static int adjustCyclesByValue(String value) {
		return value == null || value.isEmpty() ? DEFAULT_VALUE : Integer.valueOf(value);
	}
}
