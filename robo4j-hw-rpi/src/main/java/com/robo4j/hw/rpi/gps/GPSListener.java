/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.hw.rpi.gps;

import com.robo4j.hw.rpi.serial.gps.MTK3339GPS;
import com.robo4j.hw.rpi.serial.gps.MTK3339PositionEvent;
import com.robo4j.hw.rpi.serial.gps.MTK3339VelocityEvent;

/**
 * Listener to listen for GPS information.
 * 
 * @see MTK3339GPS
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public interface GPSListener {
	/**
	 * Callback for receiving position updates.
	 * 
	 * @param event
	 *            an event describing the position.
	 * @see MTK3339PositionEvent
	 */
	void onPosition(PositionEvent event);

	/**
	 * Callback for receiving velocity updates.
	 * 
	 * @param event
	 *            an event describing the velocity.
	 * @see MTK3339VelocityEvent
	 */
	void onVelocity(VelocityEvent event);
}
