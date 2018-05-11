/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.units.rpi.lcd;

import java.io.IOException;

/**
 * The interface for the demos.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface LcdDemo {
	/**
	 * The name of the demo. Keep it shorter than 13 characters.
	 * 
	 * @return the name of the demo.
	 */
	String getName();

	/**
	 *
	 * @throws IOException
	 *             exception
	 */
	void run() throws IOException;

	/**
	 * @return true if the demo is still running.
	 */
	boolean isRunning();
}
