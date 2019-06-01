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
 * Event for the velocity.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public interface VelocityEvent extends GPSEvent {
	/**
	 * Returns the measured heading in degrees.
	 * 
	 * @return the measured heading in degrees.
	 */
	float getHeading();

	/**
	 * Returns the horizontal speed in km/h.
	 * 
	 * @return the horizontal speed in km/h.
	 */
	float getGroundSpeed();
}
