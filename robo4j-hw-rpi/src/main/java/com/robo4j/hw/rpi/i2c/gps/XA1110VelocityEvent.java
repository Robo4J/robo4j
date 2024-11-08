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
package com.robo4j.hw.rpi.i2c.gps;

import com.robo4j.hw.rpi.gps.AbstractGPSEvent;
import com.robo4j.hw.rpi.gps.GPS;
import com.robo4j.hw.rpi.gps.NmeaUtils;
import com.robo4j.hw.rpi.gps.VelocityEvent;

/**
 * Velocity event.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class XA1110VelocityEvent extends AbstractGPSEvent implements VelocityEvent {
	static final String VELOCITY_TAG_GPS_PLUS_GLONASS = "$GNVTG";
	static final String VELOCITY_TAG_GPS = "$GPVTG";
	static final String VELOCITY_TAG_GLONASS = "$GLVTG";

	static final String[] ACCEPTED_MESSAGES = { VELOCITY_TAG_GLONASS, VELOCITY_TAG_GPS, VELOCITY_TAG_GPS_PLUS_GLONASS };
	private final float heading;
	private final float groundSpeed;

	public XA1110VelocityEvent(GPS source, float heading, float groundSpeed) {
		super(source);
		this.heading = heading;
		this.groundSpeed = groundSpeed;
	}

	@Override
	public float getHeading() {
		return heading;
	}

	@Override
	public float getGroundSpeed() {
		return groundSpeed;
	}

	@Override
	public String toString() {
		return String.format("VelocityEvent heading: %f, ground speed: %f ", getHeading(), getGroundSpeed());
	}

	public static boolean isAcceptedLine(String nmeaMessage) {
		if (nmeaMessage == null) {
			return false;
		}
        for (String acceptedMessage : ACCEPTED_MESSAGES) {
            if (nmeaMessage.startsWith(acceptedMessage)) {
                return true;
            }
        }
		return false;
	}

	public static VelocityEvent decode(GPS gps, String nmeaLine) {
		String[] args = nmeaLine.split(",");
		if (args.length >= 8) {
			float heading = NmeaUtils.getFloat(args[1]);
			float groundSpeed = NmeaUtils.getFloat(args[7]);
			return new XA1110VelocityEvent(gps, heading, groundSpeed);
		}
		return null;
	}
}
