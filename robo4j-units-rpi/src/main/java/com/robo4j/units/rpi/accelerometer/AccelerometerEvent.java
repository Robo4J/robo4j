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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.units.rpi.accelerometer;

import com.robo4j.math.geometry.Tuple3f;

/**
 * This event will be sent when a notification threshold has been passed.
 * 
 * FIXME(Marcus/Apr 23, 2017): If all we are doing is sending the angles (i.e.
 * no source etc) we can skip this class and save some allocation pressure.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AccelerometerEvent {
	private final Tuple3f angles;

	/**
	 * Constructor.
	 * 
	 * @param angles
	 *            the angular delta to report.
	 */
	public AccelerometerEvent(Tuple3f angles) {
		this.angles = angles;
	}

	/**
	 * @return the angular (in angular degrees) deltas since originally asking
	 *         for notifications.
	 */
	public Tuple3f getAngles() {
		return angles;
	}

	@Override
	public String toString() {
		return "Angles: " + angles == null ? "null" : angles.toString();
	}
}
