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
package com.robo4j.units.rpi.lidarlite;

public class ScanRequest {
	private final boolean continuously;
	private final float startAngle;
	private final float range;
	private final float step;

	/**
	 * 
	 * @param continuously should the unit keep scanning until told otherwise?
	 * @param servoRange how much of the full servo range should be scanned (in radians).
	 */
	public ScanRequest(boolean continuously, float startAngle, float range, float step) {
		this.continuously = continuously;
		this.startAngle = startAngle;
		this.range = range;
		this.step = step;
	}

	public boolean isContinuously() {
		return continuously;
	}

	public float getStartAngle() {
		return startAngle;
	}

	public float getRange() {
		return range;
	}

	public float getStep() {
		return step;
	}
}
