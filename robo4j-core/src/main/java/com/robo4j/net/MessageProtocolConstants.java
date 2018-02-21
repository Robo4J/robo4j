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
package com.robo4j.net;

/**
 * Protocol specific constants
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
interface MessageProtocolConstants {
	/**
	 * Serialization will be done using standard java serialization.
	 */
	int OBJECT = 0;

	/**
	 * Message is a byte.
	 */
	int BYTE = 16;

	/**
	 * Message is a short.
	 */
	int SHORT = 17;

	/**
	 * Message is a char.
	 */
	int CHAR = 18;

	/**
	 * Message is an int.
	 */
	int INT = 19;

	/**
	 * Message is a long.
	 */
	int LONG = 20;

	/**
	 * Message is a float.
	 */
	int FLOAT = 21;

	/**
	 * Message is a double.
	 */
	int DOUBLE = 22;

	/**
	 * Message is a modified UTF8.
	 */
	int MOD_UTF8 = 32;

	/**
	 * This is a Robo4J reference. Serialization will be specially handled.
	 */
	int REFERENCE = 64;

	short MAGIC = (short) 0xC0FE;
}
