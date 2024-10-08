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

/**
 * Interface for GPSes.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public interface GPS {
	/**
	 * Adds a new listener to this GPS.
	 * 
	 * @param gpsListener
	 *            the listener to listen for GPS data.
	 */
	void addListener(GPSListener gpsListener);

	/**
	 * Removes a listener from this GPS.
	 * 
	 * @param gpsListener
	 *            the listener to remove.
	 */
	void removeListener(GPSListener gpsListener);

	/**
	 * Starts receiving updates from the GPS.
	 */
	void start();

	/**
	 * Shuts down this GPS.
	 */
	void shutdown();

}
