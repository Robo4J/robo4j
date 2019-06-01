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
 * Event for the position.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public interface PositionEvent extends GPSEvent {

	/**
	 * Returns the 2D location on earth.
	 * 
	 * @return the 2D location on earth.
	 * 
	 * @see Location
	 */
	Location getLocation();

	/**
	 * Returns the antenna altitude above/below mean sea level in meters.
	 * 
	 * @return the antenna altitude above/below mean sea level in meters.
	 */
	float getAltitude();

	/**
	 * Returns the horizontal dilution of precision.
	 * 
	 * @see AccuracyCategory
	 * 
	 * @return the horizontal dilution of precision.
	 */
	float getHorizontalDilutionOfPrecision();

	/**
	 * Returns the fix quality.
	 * 
	 * @return the fix quality.
	 */
	FixQuality getFixQuality();

	/**
	 * Returns the number of satellites used.
	 * 
	 * @return the number of satellites used.
	 */
	int getNumberOfSatellites();

	/**
	 * Returns the geoidal separation.
	 * 
	 * @return the geoidal separation.
	 */
	float getGeoidalSeparation();
}
