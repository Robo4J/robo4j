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
package com.robo4j.units.rpi.gyro;

import com.robo4j.math.geometry.Float3D;

/**
 * This event will be sent when a notification threshold has been passed.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class GyroEvent {
	private final Float3D angles;

	public GyroEvent(Float3D angles) {
		this.angles = angles;
	}

	public Float3D getAngles() {
		return angles;
	}
	
	public String toString() {
		return "Angles: " + angles == null ? "null" : angles.toString();
	}
}
