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
	int OBJECT = 0;
	int BYTE = 16;
	int SHORT = 17;
	int CHAR = 18;
	int INT = 19;
	int LONG = 20;
	int FLOAT = 21;
	int DOUBLE = 22;
	int MOD_UTF8 = 32;
	short MAGIC = (short) 0xC0FE;
}
