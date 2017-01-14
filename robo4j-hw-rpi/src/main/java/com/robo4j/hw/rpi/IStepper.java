/*
 * Copyright (C) 2017. Miroslav Wengner, Marcus Hirt
 * This IStepper.java  is part of robo4j.
 * module: robo4j-hw-rpi
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.hw.rpi;

import java.io.IOException;

/**
 * The interface for a stepper motor.
 * 
 * @author Marcus Hirt
 */
public interface IStepper {
	/**
	 * Rotates the stepper motor the specified number of degrees forwards (if
	 * positive) or backwards (if negative).
	 * 
	 * @param radians
	 *            the number of radians to move the stepper motor.
	 * @throws IOException
	 */
	void rotate(double radians) throws IOException;
}
