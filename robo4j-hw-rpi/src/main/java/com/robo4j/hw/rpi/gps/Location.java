/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
 * A 2D location on earth.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class Location {
	private static final String STR_DEGREE = "\u00B0";
	private static final String STR_MINUTE = "'"; // "\u2032";
	private static final String STR_SECOND = "\""; // \u2033";

	private final float latitude;
	private final float longitude;

	/**
	 * Creates a new location.
	 * 
	 * @param latitude
	 *            the latitude in decimal degrees.
	 * @param longitude
	 *            the longitude in decimal degrees.
	 */
	public Location(float latitude, float longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * @return the decimal degree for the latitude.
	 */
	public float getLatitude() {
		return latitude;
	}

	/**
	 * @return the decimal degree for the longitude.
	 */
	public float getLongitude() {
		return longitude;
	}

	/**
	 * Returns the coordinates as a String in degrees, minutes and seconds format.
	 * 
	 * @return the coordinates as a String in degrees, minutes and seconds format.
	 */
	public String asDMS() {
		return String.format("%s%s %s%s", toDMS(latitude), latitude > 0 ? "N" : "S", toDMS(longitude), longitude > 0 ? "E" : "W");
	}

	private Object toDMS(float coordinate) {
		int deg = Math.abs((int) coordinate);
		int minute = Math.abs((int) (coordinate * 60) % 60);
		int second = Math.abs((int) (coordinate * 3600) % 60);
		return String.format("%d%s%d%s%d%s", deg, STR_DEGREE, minute, STR_MINUTE, second, STR_SECOND);
	}

	@Override
	public String toString() {
		return asDMS();
	}
}
