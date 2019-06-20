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
 * MatrixRotation allows to select matrix start x,y position
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum MatrixRotation {
	//@formatter:off
	NONE		(0, "none"),
    ONE 		(1,"default setup to pins"),
    TWO 		(2, "left rotation to pins"),
    THREE	 	(3,"right rotation to pins"),
    FOUR    	(4,"switch to pins, x vertical"),
    FIVE    	(5,"switch to pins, y horizontal")
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
