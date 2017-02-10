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
public final class HttpUtils {

	public static final String HTTP_HEADER_OK = "HTTP/1.0 200 OK";
	public static final String HTTP_HEADER_NOT = "HTTP/1.0 501 Not Implemented";
	public static final String HTTP_HEADER_NOT_ALLOWED = "HTTP/1.0 405 Method Not Allowed";
	private static final String NEXT_LINE = "\r\n";

	public static String setHeader(String responseCode, int length) throws IOException {
		return new StringBuilder(ConstantUtil.EMPTY_STRING).append(responseCode).append(NEXT_LINE).append("Date: ")
				.append(LocalDateTime.now()).append(NEXT_LINE).append("Server: robo4j-client").append(NEXT_LINE)
				.append("Content-length: ").append(length).append(NEXT_LINE)
				.append("Content-type: text/html; charset=utf-8").append(NEXT_LINE).append(NEXT_LINE).toString();
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
