/*
 * Copyright (C) 2017. Miroslav Wengner, Marcus Hirt
 * This AccuracyCategory.java  is part of robo4j.
 * module: robo4j-hw-rpi
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.hw.rpi.serial.gps;

/**
 * A user friendly categorization of dilution of precision.
 * http://en.wikipedia.org/wiki/Dilution_of_precision_(GPS)
 * 
 * @author Marcus Hirt
 */
public enum AccuracyCategory {
	//@formatter:off
	IDEAL		(1, "Ideal",
					"This is the highest possible confidence level to be used for applications demanding the highest possible precision at all times."),
	EXCELLENT	(2, "Excellent",
					"At this confidence level, positional measurements are considered accurate enough to meet all but the most sensitive applications."),
	GOOD		(5,	"Good",
					"Represents a level that marks the minimum appropriate for making business decisions. Positional measurements could be used to make reliable in-route navigation suggestions to the user."),
	MODERATE	(10,"Moderate",
					"Positional measurements could be used for calculations, but the fix quality could still be improved. A more open view of the sky is recommended."),
	FAIR		(20,"Fair",
					"Represents a low confidence level. Positional measurements should be discarded or used only to indicate a very rough estimate of the current location."),
	POOR		(100,"Poor",
					"At this level, measurements are inaccurate by as much as 300 meters with a 6 meter accurate device (50 DOP × 6 meters) and should be discarded.");
	//@formatter:on

	private final int dop;
	private String name;
	private String description;

	private AccuracyCategory(int dop, String name, String description) {
		this.dop = dop;
		this.name = name;
		this.description = description;
	}

	/**
	 * Returns the user friendly name of the accuracy.
	 * 
	 * @return the user friendly name of the accuracy.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a longer description explaining the accuracy.
	 * 
	 * @return a longer description explaining the accuracy.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Looks up an Accuracy from a Dilution of Precision value.
	 * 
	 * @param dop
	 *            the dilution of precision measurement to get an Accuracy from.
	 * @return the Accuracy corresponding to the dilution of precision.
	 */
	public static AccuracyCategory fromDOP(float dop) {
		for (AccuracyCategory a : values()) {
			if (a.dop >= dop) {
				return a;
			}
		}
		return POOR;
	}

}
