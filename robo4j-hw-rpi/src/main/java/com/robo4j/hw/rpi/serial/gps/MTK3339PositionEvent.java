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
package com.robo4j.hw.rpi.serial.gps;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.robo4j.hw.rpi.gps.AbstractGPSEvent;
import com.robo4j.hw.rpi.gps.AccuracyCategory;
import com.robo4j.hw.rpi.gps.FixQuality;
import com.robo4j.hw.rpi.gps.GPS;
import com.robo4j.hw.rpi.gps.Location;
import com.robo4j.hw.rpi.gps.NmeaUtils;
import com.robo4j.hw.rpi.gps.PositionEvent;

/**
 * A GPS event describing position data.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class MTK3339PositionEvent extends AbstractGPSEvent implements PositionEvent {
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
	public enum FixQualityEnum {
		INVALID, GPS, DGPS, PPS, RTK, FLOAT_RTK, ESTIMATED, MANUAL, SIMULATION;

		public static FixQualityEnum getFixQuality(int code) {
			for (FixQualityEnum fq : values()) {
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
	public MTK3339PositionEvent(GPS source, String data) {
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
	 * @return accuracy category
	 */
	public AccuracyCategory getAccuracyCategory() {
		return accuracyCategory;
	}

	/**
	 * Returns an estimate of the max error for the location in this
	 * measurement, in meters.
	 *
	 * @return max error estimate
	 */
	public float getMaxError() {
		return hdop * MTK3339GPS.UNAIDED_POSITION_ACCURACY;
	}

	@Override
	public float getGeoidalSeparation() {
		return geoidSeparation;
	}

	@Override
	public String toString() {
		return "Coordinates: " + getLocation() + " alt: " + getAltitude() + "m altWGS84: " + getElipsoidAltitude() + "m FixQuality: "
				+ getFixQuality() + " #sat: " + getNumberOfSatellites() + " max error: " + getMaxError() + "m accuracy category:"
				+ getAccuracyCategory().getName();
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
		float latitude = NmeaUtils.parseNmeaFormatCoordinate(args[2]);
		if ("S".equals(args[3])) {
			latitude *= -1;
		}
		float longitude = NmeaUtils.parseNmeaFormatCoordinate(args[4]);
		if ("W".equals(args[5])) {
			longitude *= -1;
		}
		location = new Location(latitude, longitude);
		numberOfSatellites = NmeaUtils.getInt(args[7]);
		hdop = NmeaUtils.getFloat(args[8]);
		accuracyCategory = AccuracyCategory.fromDOP(hdop);
		altitude = NmeaUtils.getFloat(args[9]);
		geoidSeparation = NmeaUtils.getFloat(args[11]);
		FixQualityEnum fixQualityEnum = FixQualityEnum.getFixQuality(Integer.parseInt(args[6]));
		fixQuality = new FixQuality(fixQualityEnum.ordinal(), fixQualityEnum.toString());
	}
}
