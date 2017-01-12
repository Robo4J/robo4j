/*
 * Copyright (C) 2014-2016, Marcus Hirt
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
package com.robo4j.hw.rpi.serial.gps;

/**
 * An event describing heading and speed relative to the ground.
 * 
 * @author Marcus Hirt
 */
public final class VelocityEvent extends GPSEvent {
	private float trueTrackMadeGood = Float.NaN;
	private float magneticTrackMadeGood = Float.NaN;
	private float groundSpeed = Float.NaN;

	/**
	 * Constructs a new VelocityEvent.
	 *
	 * @param source
	 *            the GPS from which the event originated.
	 * @param data
	 *            the raw GPS data.
	 */
	public VelocityEvent(GPS source, String data) {
		super(source);
		parse(data);
	}

	/**
	 * Returns the measured heading in degrees.
	 * 
	 * @return the measured heading in degrees.
	 */
	public float getTrueTrackMadeGood() {
		return trueTrackMadeGood;
	}

	/**
	 * Returns the measured magnetic heading. According to the chip manual this
	 * seems to "need GlobalTop Customization Service".
	 * 
	 * @return the measured magnetic heading.
	 */
	public float getMagneticTrackMadeGood() {
		return magneticTrackMadeGood;
	}

	/**
	 * Returns the horizontal speed in km/h.
	 * 
	 * @return the horizontal speed in km/h.
	 */
	public float getGroundSpeed() {
		return groundSpeed;
	}

	@Override
	public String toString() {
		return String.format("True: %.1f\u00B0 Magnetic: %.1f\u00B0 Speed: %.1f km/h", getTrueTrackMadeGood(), getMagneticTrackMadeGood(),
				getGroundSpeed());
	}

	protected void parse(String data) {
		String[] args = data.split(",");
		if (args.length >= 8) {
			trueTrackMadeGood = getFloat(args[1]);
			magneticTrackMadeGood = getFloat(args[3]);
			groundSpeed = getFloat(args[7]);
		}
	}

	private float getFloat(String string) {
		if (string == null || "".equals(string)) {
			return Float.NaN;
		}
		return Float.parseFloat(string);
	}
}
