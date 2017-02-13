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
package com.robo4j.core.client.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.robo4j.core.client.request.RoboBasicMapEntry;
import com.robo4j.core.util.ConstantUtil;

/**
 * Basic Http constants and utils methods
 *
 * @author Miroslav Wengner (@miragemiko)
 */
public final class RoboHttpUtils {

	public static final int DEFAULT_THREAD_POOL_SIZE = 2;
	public static final String HTTP_HEADER_OK = "HTTP/1.1 200 OK\n";
	public static final int _DEFAULT_PORT = 8042;
	public static final String METHOD_GET = "GET";
	public static final String _EMPTY_STRING = "";
	public static final String HTTP_COMMANDS = "commands";
	public static final String HTTP_COMMAND = "command";
	public static final String HTTP_HEADER_NOT = "HTTP/1.0 501 Not Implemented";
	public static final String HTTP_HEADER_NOT_ALLOWED = "HTTP/1.0 405 Method Not Allowed";
	public static final int KEEP_ALIVE_TIME = 10;
	private static final String NEXT_LINE = "\r\n";
	private static final String SPACE = "\u0020";


	public static String setHeader(String responseCode, int length) throws IOException {
		//@formatter:off
		return new StringBuilder(ConstantUtil.EMPTY_STRING).append(responseCode).append(NEXT_LINE)
				.append("Date:").append(SPACE).append(LocalDateTime.now()).append(NEXT_LINE)
				.append("Server:").append(SPACE).append("robo4j-client").append(NEXT_LINE)
				.append("Content-length:").append(SPACE).append(length).append(NEXT_LINE)
				.append("Content-type:").append(SPACE).append("text/html;").append(SPACE).append("charset=utf-8")
				.append(NEXT_LINE).append(NEXT_LINE)
				.toString();
		//@formatter:on
	}

	public static String createResponseHeader(String host) {
		//@formatter:off
		return new StringBuilder("Host:").append(SPACE).append(host).append(NEXT_LINE)
				.append("Connection:").append(SPACE).append("keep-alive").append(NEXT_LINE)
				.append("Cache-Control:").append(SPACE).append("no-cache").append(NEXT_LINE)
				.append("User-Agent:").append(SPACE).append("Robo4J-HttpClient").append(NEXT_LINE)
				.append("Accept:").append(SPACE).append("*/*").append(NEXT_LINE)
				.append("Accept-Encoding:").append(SPACE).append("gzip, deflate, sdch, br").append(NEXT_LINE)
				.append("Accept-Language:").append(SPACE).append("en-US,en;q=0.8")
				.append(NEXT_LINE)
				.toString();
		//@formatter:on
	}

	public static String createGetRequest(String host, String message) {
		//@formatter:off
		return new StringBuilder("GET").append(SPACE).append(message).append(SPACE).append("HTTP/1.1").append(NEXT_LINE)
				.append(createResponseHeader(host))
				.toString();
		//@formatter:on
	}

	public static String correctLine(String line) {
		return line == null ? ConstantUtil.EMPTY_STRING : line;
	}

	public static Map<String, String> parseURIQueryToMap(final String uriQuery, final String delimiter) {
		//@formatter:off
		return Stream.of(uriQuery.split(delimiter))
				.filter(e -> !e.isEmpty())
				.map(RoboBasicMapEntry::new)
				.collect(Collectors.toMap(RoboBasicMapEntry::getKey, RoboBasicMapEntry::getValue));
		//@formatter:on
	}

}
