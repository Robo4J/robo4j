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
import com.robo4j.util.Utf8Constant;

import java.util.Objects;

/**
 * Basic Http constants and utils methods
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class RoboHttpUtils {

	public static final String NEW_LINE_MAC = "\r";
	public static final String NEW_LINE_UNIX = "\n";

	public static final int DEFAULT_PORT = 8042;
	public static final int DEFAULT_UDP_PORT = 9042;
	public static final String HTTP_PROPERTY_PROTOCOL = "protocol";
	public static final String PROPERTY_TARGET = "target";
	public static final String PROPERTY_HOST = "host";
	public static final String PROPERTY_SOCKET_PORT = "port";
	public static final String PROPERTY_CODEC_REGISTRY = "codecRegistry";
	public static final String PROPERTY_CODEC_PACKAGES = "packages";
	public static final String PROPERTY_UNIT_PATHS_CONFIG = "unitPathsConfig";
	public static final String PROPERTY_BUFFER_CAPACITY = "bufferCapacity";
	public static final String PROPERTY_BYTE_BUFFER = "byteBuffer";
	public static final String PROPERTY_TIMEOUT = "timeout";

	public static void decorateByNewLine(StringBuilder sb) {
		sb.append(NEW_LINE_MAC).append(NEW_LINE_UNIX);
	}

	public static String correctLine(String line) {
		return line == null ? StringConstants.EMPTY : line;
	}

	/**
	 * create host header note
	 *
	 * @param host
	 *            desired host
	 * @param port
	 *            default port is 80
	 * @return host string
	 */
	public static String createHost(String host, Integer port) {
		Objects.requireNonNull(host, "host not available");
		Objects.requireNonNull(host, "port not available");
		return new StringBuilder(host).append(Utf8Constant.UTF8_COLON).append(port).toString();
	}

	/**
	 * report time in moment M from start time
	 *
	 * @param clazz
	 *            desired class
	 * @param message
	 *            message
	 * @param start
	 *            start time
	 */
	public static void printMeasuredTime(Class<?> clazz, String message, long start) {
		System.out.println(String.format("%s message: %s duration: %d%n", clazz.getSimpleName(), message,
				System.currentTimeMillis() - start));
	}

	// TODO: 1/26/18 (miro
	public static boolean validatePackages(String packages) {
		if (packages == null) {
			return false;
		}
		for (int i = 0; i < packages.length(); i++) {
			char c = packages.charAt(i);
			if (Character.isWhitespace(c)) {
				return false;
			}
		}
		return true;
	}

}
