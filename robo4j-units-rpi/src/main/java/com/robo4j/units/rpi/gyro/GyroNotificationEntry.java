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

import com.robo4j.core.RoboReference;
import com.robo4j.math.geometry.Tuple3f;

/**
 * Internal interface for book keeping required for the gyro unit.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
interface GyroNotificationEntry {
	/**
	 * @return the current delta angles.
	 */
	Tuple3f getDelta();

	/**
	 * @return the robo unit to get the notifications.
	 */
	RoboReference<GyroEvent> getTarget();

	/**
	 * @return should this be a one-off, or should it be continuously running.
	 */
	boolean isContinuous();

	/**
	 * @param data
	 *            the delta measurement to add to the current angles.
	 */
	void addDelta(Tuple3f data);
}
