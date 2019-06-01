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
package com.robo4j.hw.rpi.i2c.gps;

import com.robo4j.hw.rpi.gps.AbstractGPSEvent;
import com.robo4j.hw.rpi.gps.FixQuality;
import com.robo4j.hw.rpi.gps.GPS;
import com.robo4j.hw.rpi.gps.Location;
import com.robo4j.hw.rpi.gps.NmeaUtils;
import com.robo4j.hw.rpi.gps.PositionEvent;

/**
 * Position event.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class XA1110PositionEvent extends AbstractGPSEvent implements PositionEvent {
	static final String POSITION_TAG_GPS_PLUS_GLONASS = "$GNGGA";
	static final String POSITION_TAG_GPS = "$GPGGA";
	static final String POSITION_TAG_GLONASS = "$GLGGA";

	static final String[] ACCEPTED_MESSAGES = { POSITION_TAG_GLONASS, POSITION_TAG_GPS, POSITION_TAG_GPS_PLUS_GLONASS };

	private final Location location;
	private final float altitude;
	private final float hdop;
	private final FixQuality fixQuality;
	private final int numberOfSats;
	private final float geoidalSeparation;

	/**
	 * The quality of the GPS fix.
	 */
	public enum FixQualityEnum {
		NA("Not available"), GPS("GPS fix"), DGPS("Differential GPS fix");

		String description;

		FixQualityEnum(String description) {
			this.description = description;
		}

		public static FixQualityEnum getFixQuality(int code) {
			for (FixQualityEnum fq : values()) {
				if (fq.ordinal() == code) {
					return fq;
				}
			}
			return NA;
		}

		public String getDescription() {
			return description;
		}
	}

	public XA1110PositionEvent(GPS source, FixQuality quality, Location location, float altitude, int numberOfSats, float hdop,
			float geoidSeparation) {
		super(source);
		this.fixQuality = quality;
		this.location = location;
		this.altitude = altitude;
		this.numberOfSats = numberOfSats;
		this.hdop = hdop;
		this.geoidalSeparation = geoidSeparation;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public float getAltitude() {
		return altitude;
	}

	@Override
	public FixQuality getFixQuality() {
		return fixQuality;
	}

	@Override
	public float getHorizontalDilutionOfPrecision() {
		return hdop;
	}

	@Override
	public int getNumberOfSatellites() {
		return numberOfSats;
	}

	@Override
	public float getGeoidalSeparation() {
		return geoidalSeparation;
	}

	public static PositionEvent decode(GPS source, String nmeaMessage) {

		String[] args = nmeaMessage.split(",");
		// Don't care about the time of the fix for now...
		float latitude = NmeaUtils.parseNmeaFormatCoordinate(args[2]);
		if ("S".equals(args[3])) {
			latitude *= -1;
		}
		float longitude = NmeaUtils.parseNmeaFormatCoordinate(args[4]);
		if ("W".equals(args[5])) {
			longitude *= -1;
		}
		Location location = new Location(latitude, longitude);
		int numberOfSatellites = NmeaUtils.getInt(args[7]);
		float hdop = NmeaUtils.getFloat(args[8]);
		float altitude = NmeaUtils.getFloat(args[9]);
		float geoidSeparation = NmeaUtils.getFloat(args[11]);
		FixQualityEnum fixQualityEnum = FixQualityEnum.getFixQuality(Integer.parseInt(args[6]));
		FixQuality fixQuality = new FixQuality(fixQualityEnum.ordinal(), fixQualityEnum.toString());

		return new XA1110PositionEvent(source, fixQuality, location, altitude, numberOfSatellites, hdop, geoidSeparation);
	}

	public static boolean isAcceptedLine(String nmeaMessage) {
		if (nmeaMessage == null) {
			return false;
		}
		for (int i = 0; i < ACCEPTED_MESSAGES.length; i++) {
			if (nmeaMessage.startsWith(ACCEPTED_MESSAGES[i])) {
				return true;
			}
		}
		return false;
	}

	public String toString() {
		return String.format("PositionEvent loc: %s alt: %f sats: %d", location.toString(), getAltitude(), getNumberOfSatellites()); 
	}
}
