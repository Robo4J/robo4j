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
package com.robo4j.hw.rpi;

import java.io.IOException;

/**
 * Abstraction for a servo.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public interface Servo {
	/**
	 * If set to true, input will be treated as inverted for this servo.
	 *
	 * @return boolean value
	 */
	boolean isInverted();

	/**
	 * If set to true, input will be treated as inverted for this servo.
	 * 
	 * @param invert
	 *            boolean value
	 */
	void setInverted(boolean invert);

	/**
	 * Sets the trim. This will translate the entire output curve.
	 * 
	 * @param trim
	 *            the absolute position to set the trim to.
	 */
	void setTrim(float trim);

	/**
	 * @return the trim settings for the servo.
	 */
	float getTrim();

	/**
	 * Returns the last input used for this servo.
	 * 
	 * @return the last input used for this servo.
	 * @throws IOException
	 *             possible exception
	 */
	float getInput() throws IOException;

	/**
	 * Sets the normalized input to this servo, between -1 (min) and 1 (max).
	 * 
	 * @param input
	 *            normalized input between -1 and 1.
	 * 
	 * @throws IOException
	 *             if there was a problem communicating with the device.
	 */
	void setInput(float input) throws IOException;
}
