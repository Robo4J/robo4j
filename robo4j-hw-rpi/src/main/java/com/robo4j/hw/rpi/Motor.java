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
package com.robo4j.hw.rpi;

import java.io.IOException;

/**
 * The interface for a normal DC motor.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public interface Motor {
	/**
	 * Returns the normalized speed of the motor.
	 * 
	 * @return the normalized speed of the motor [-1.0, 1.0]
	 * @throws IOException
	 */
	float getSpeed() throws IOException;

	/**
	 * Sets the normalized speed of the motor.
	 * 
	 * @param speed
	 *            the normalized speed of the motor, from maximum speed reverse
	 *            (-1.0f) via halted (0) to maximum ahead (1.0f).
	 * @throws IOException
	 */
	void setSpeed(float speed) throws IOException;
}
