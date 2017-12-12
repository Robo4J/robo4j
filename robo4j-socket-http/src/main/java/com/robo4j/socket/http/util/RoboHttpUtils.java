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
package com.robo4j.socket.http.util;

import com.robo4j.util.StringConstants;

import static com.robo4j.util.Utf8Constant.UTF8_COLON;

/**
 * Basic Http constants and utils methods
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class RoboHttpUtils {

	public static final String NEW_LINE_MAC = "\r";
	public static final String NEW_LINE_UNIX = "\n";
	public static final CharSequence CHAR_QUOTATION_MARK = "\"";
	public static final CharSequence CHAR_COLON = ":";
	public static final CharSequence CHAR_CURLY_BRACKET_LEFT = "{";
	public static final CharSequence CHAR_CURLY_BRACKET_RIGHT = "}";
	public static final CharSequence CHAR_SQUARE_BRACKET_LEFT = "[";
	public static final CharSequence CHAR_SQUARE_BRACKET_RIGHT = "]";
	public static final CharSequence CHAR_COMMA = ",";
	public static final int DEFAULT_PORT = 8042;
	public static final String HTTP_TARGETS = "targets";
	public static final String HTTP_PROPERTY_HOST = "host";
	public static final String HTTP_PROPERTY_PORT = "port";
	public static final String HTTP_PROPERTY_BUFFER_CAPACITY = "bufferCapacity";
	public static final String HTTP_PROPERTY_RESPONSE_HANLDER = "responseHandler";

	public static void decorateByNewLine(StringBuilder sb){
		sb.append(NEW_LINE_MAC).append(NEW_LINE_UNIX);
	}


	public static String correctLine(String line) {
		return line == null ? StringConstants.EMPTY : line;
	}

	public static String createHostWithPort(String host, Object port) {

		return port == null || Integer.valueOf(port.toString()) == 80 ? host
				: new StringBuilder(host).append(UTF8_COLON).append(port).toString();
	}

	public static String createHost(String host){
		return createHost(host, null);
	}

	public static String createHost(String host, Integer port){
		StringBuilder sb = new StringBuilder(host);
		if(port != null && port != 80 && port != 443){
			sb.append(CHAR_COLON)
					.append(port);
		}
		return sb.toString();
	}

	/**
	 * report time in moment M from start time
	 *
	 * @param message
	 *            message
	 * @param start
	 *            start time
	 */
	public static void printMeasuredTime(Class<?> clazz, String message, long start) {
		System.out.println(String.format("%s message: %s duration: %d%n", clazz.getSimpleName(), message,
				System.currentTimeMillis() - start));
	}

}
