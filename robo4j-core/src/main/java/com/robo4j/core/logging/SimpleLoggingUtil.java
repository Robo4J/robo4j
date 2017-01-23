/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This SimpleLoggingUtil.java  is part of robo4j.
 *
 *  robo4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  robo4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.logging;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Simple toolkit for logging
 * 
 * @author Miroslav Wengner (@miragemiko)
 * @since 19.07.2016
 */
public final class SimpleLoggingUtil {
	private static final String SPACE = " ";
	private static final String EMPTY = "";

	public static void print(Class<?> clazz, String message) {
		Logger.getLogger(clazz.getName()).log(Level.INFO, message);
	}

	public static void debug(Class<?> clazz, String message) {
		Logger.getLogger(clazz.getName()).log(Level.FINER, message);
	}

	public static void debug(Class<?> clazz, String... message){
		debug(clazz, clazz.getSimpleName() + " : " +
				Stream.of(message).reduce(EMPTY,  (l, r) -> l.concat(SPACE).concat(r)));
	}

	public static void error(Class<?> clazz, String message) {
		Logger.getLogger(clazz.getName()).log(Level.SEVERE, message);
	}

	public static void error(Class<?> clazz, String string, Exception e) {
		Logger.getLogger(clazz.getName()).log(Level.SEVERE, string, e);
	}

	public static void debug(Class<?> clazz, String message, Exception e) {
		Logger.getLogger(clazz.getName()).log(Level.INFO, message, e);
	}
}
