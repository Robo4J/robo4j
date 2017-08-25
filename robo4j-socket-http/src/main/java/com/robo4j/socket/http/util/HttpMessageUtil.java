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

package com.robo4j.socket.http.util;

/**
 * Util class for http message
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class HttpMessageUtil {

	public static final String SPACE = "\u0020";
	public static final String NEXT_LINE = "\r\n";
	public static final String COLON = ":";

	/**
	 * The HTTP separator characters. Defined in RFC 2616, section 2.2
	 */
	private static final String HTTP_SEPARATORS = " ,;=()<>@:\\\"/[]?{}\t";

	public static final int METHOD_KEY_POSITION = 0, URI_VALUE_POSITION = 1, VERSION_POSITION = 2, HTTP_HEADER_SEP = 9;

	public static String getHttpSeparator(int position) {
		return Character.toString(HTTP_SEPARATORS.charAt(position));
	}
}
