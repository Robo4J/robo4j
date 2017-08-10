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
package com.robo4j.hw.rpi.serial.gps;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A GPS event describing position data.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@SuppressWarnings(value = "rawtypes")
public final class PositionEvent<R> extends GPSEvent {
	private Date time;
	private FixQuality fixQuality;
	private Location location;
	private float altitude = Float.NaN;
	private float geoidSeparation = Float.NaN;
	private int numberOfSatellites = -1;
	private float hdop;
	private AccuracyCategory accuracyCategory;

	/**
	 * The quality of the GPS fix.
	 */
	public enum FixQuality {
		INVALID, GPS, DGPS, PPS, RTK, FLOAT_RTK, ESTIMATED, MANUAL, SIMULATION;

		public static FixQuality getFixQuality(int code) {
			for (FixQuality fq : values()) {
				if (fq.ordinal() == code) {
					return fq;
				}
			}
			return INVALID;
		}
	}

	/**
	 * Creates a new position event
	 * 
	 * @param source
	 *            the GPS that was the source of the event.
	 * @param data
	 *            the raw GPS data.
	 */
	public PositionEvent(GPS source, String data) {
		super(source);
		parse(data);
	}

	/**
	 * Returns the time, zulu, for the fix. Note that only the time part is
	 * valid, not the date.
	 * 
	 * @return the time, zulu, for the fix. Note that only the time part is
	 *         valid, not the date.
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * Returns the FixQuality at the time for this position event.
	 * 
	 * @return the FixQuality at the time for this position event.
	 * 
	 * @see FixQuality
	 */
	public FixQuality getFixQuality() {
		return fixQuality;
	}

	/**
	 * Returns the 2D location on earth.
	 * 
	 * @return the 2D location on earth.
	 * 
	 * @see Location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Returns the antenna altitude above/below mean sea level.
	 * 
	 * @return the antenna altitude above/below mean sea level.
	 */
	public float getAltitude() {
		return altitude;
	}

	/**
	 * Returns the antenna altitude above/below the ellipsoid (WGS84).
	 * 
	 * @return the antenna altitude above/below the ellipsoid (WGS84).
	 */
	public float getElipsoidAltitude() {
		return getAltitude() + geoidSeparation;
	}

	/**
	 * Returns the number of satellites used.
	 * 
	 * @return the number of satellites used.
	 */
	public int getNumberOfSatellites() {
		return numberOfSatellites;
	}

	/**
	 * Returns the horizontal dilution of precision.
	 * 
	 * @return the horizontal dilution of precision.
	 */
	public float getHorizontalDilutionOfPrecision() {
		return hdop;
	}

	/**
	 * Returns a more user friendly version of the dilution of precision.
	 * 
	 * @return
	 */
	public AccuracyCategory getAccuracyCategory() {
		return accuracyCategory;
	}
	
	/**
	 * Returns an estimate of the max error for the location in this measurement, in meters.
	 */
	public float getMaxError() {
		return hdop * GPS.UNAIDED_POSITION_ACCURACY;
	}

	@SuppressWarnings("unchecked")
	@Override
	public R visit(GPSVisitor visitor) {
		return (R)visitor.visit(this);
	}

	@Override
	public String toString() {
		return "Coordinates: " + getLocation() + " alt: " + getAltitude() + "m altWGS84: " + getElipsoidAltitude() + "m FixQuality: "
				+ getFixQuality() + " #sat: " + getNumberOfSatellites() + " max error: " + getMaxError() + "m accuracy category:" + getAccuracyCategory().getName();
	}

	protected void parse(String data) {
		String[] args = data.split(",");
		SimpleDateFormat format = new SimpleDateFormat("HHmmss");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		try {
			time = format.parse(args[1]);
		} catch (ParseException e) {
			// log
		}
		float latitude = parseCrazyHybridFormat(args[2]);
		if ("S".equals(args[3])) {
			latitude *= -1;
		}
		float longitude = parseCrazyHybridFormat(args[4]);
		if ("W".equals(args[5])) {
			longitude *= -1;
		}
		location = new Location(latitude, longitude);
		numberOfSatellites = getInt(args[7]);
		hdop = getFloat(args[8]);
		accuracyCategory = AccuracyCategory.fromDOP(hdop);
		altitude = getFloat(args[9]);
		geoidSeparation = getFloat(args[11]);
		fixQuality = FixQuality.getFixQuality(Integer.parseInt(args[6]));
	}

	private int getInt(String string) {
		if (string == null || "".equals(string)) {
			return -1;
		}
		return Integer.parseInt(string);
	}

	private float getFloat(String string) {
		if (string == null || "".equals(string)) {
			return Float.NaN;
		}
		return Float.parseFloat(string);
	}

	private static float parseCrazyHybridFormat(String string) {
		int index = string.indexOf('.');
		if (index < 0) {
			return Float.NaN;
		}
		float minutes = Float.parseFloat(string.substring(index - 2));
		float degrees = Integer.parseInt(string.substring(0, index - 2));
		return degrees + minutes / 60.0f;
	}
}
