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
 * MatrixRotation allows to select 2D matrix start Point(x,y) and axes direction
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum MatrixRotation {
	//@formatter:off
	NONE				(0, "none"),
    DEFAULT_X_Y	 		(1,"default setup to pins"),
    RIGHT_90 			(2, "90d to default, right"),
    RIGHT_180	 		(3,"180d to default, right"),
    RIGHT_270    		(4,"240d to default, right"),
	INVERSION    		(5,"default inversion"),
	LEFT_90				(6,"90d  to default, left"),
	LEFT_180 			(7, "180d to default, left "),
	LEFT_270			(8, "270d to default, left")
    ;
    //@formatter:on

	private int id;
	private final String note;

	MatrixRotation(int id, String note) {
		this.id = id;
		this.note = note;
	}

	public String getNote() {
		return note;
	}

	public static MatrixRotation getById(int code) {
		for (MatrixRotation r : values()) {
			if (code == r.id) {
				return r;
			}
		}
		return NONE;
	}
}
