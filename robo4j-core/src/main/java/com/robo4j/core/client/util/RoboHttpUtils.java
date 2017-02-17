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
import com.robo4j.http.HttpHeaderNames;
import com.robo4j.http.util.HttpFirstLineBuilder;
import com.robo4j.http.util.HttpHeaderBuilder;
import com.robo4j.http.util.HttpMessageUtil;

/**
 * Basic Http constants and utils methods
 *
 * @author Miroslav Wengner (@miragemiko)
 */
public final class RoboHttpUtils {

	// private static final String SPACE = "\u0020";
	// private static final String NEXT_LINE = "\r\n";
	public static final String NEW_LINE = "\n";
	public static final String HTTP_VERSION = "HTTP/1.1";
	public static final String HTTP_HEADER_OK = HttpFirstLineBuilder.Build(HTTP_VERSION).add("200")
			.add("OK").build();
	public static final int DEFAULT_THREAD_POOL_SIZE = 2;
	public static final int _DEFAULT_PORT = 8042;
	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";
	public static final String _EMPTY_STRING = "";
	public static final String COLON = ":";
	public static final String HTTP_COMMANDS = "commands";
	public static final String HTTP_COMMAND = "command";
	public static final String HTTP_HEADER_NOT = "HTTP/1.1 501 Not Implemented";
	public static final String HTTP_HEADER_NOT_ALLOWED = "HTTP/1.1 405 Method Not Allowed";
	public static final int KEEP_ALIVE_TIME = 10;



	public static String setHeader(String responseCode, int length) throws IOException {
		//@formatter:off
		return HttpHeaderBuilder.Build()
				.add(ConstantUtil.EMPTY_STRING, responseCode)
				.add(HttpHeaderNames.DATE, LocalDateTime.now().toString())
				.add(HttpHeaderNames.SERVER, "Robo4J-client")
				.add(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(length))
				.add(HttpHeaderNames.CONTENT_TYPE, "text/html;".concat(HttpMessageUtil.SPACE).concat("charset=utf-8"))
				.build();
		//@formatter:on
	}

	public static String createResponseHeader(String host) {
		//@formatter:off
		return  HttpHeaderBuilder.Build()
				.add(HttpHeaderNames.HOST, host)
				.add(HttpHeaderNames.CONNECTION, "keep-alive")
				.add(HttpHeaderNames.CACHE_CONTROL, "no-cache")
				.add(HttpHeaderNames.USER_AGENT, "Robo4J-HttpClient")
				.add(HttpHeaderNames.ACCEPT, "*/*")
				.add(HttpHeaderNames.ACCEPT_ENCODING, "gzip, deflate, sdch, br")
				.add(HttpHeaderNames.ACCEPT_LANGUAGE, "en-US,en;q=0.8")
				.build();


//		return new StringBuilder(HttpHeaderNames.HOST).append(COLON).append(SPACE).append(host).append(NEXT_LINE)
//				.append(HttpHeaderNames.ACCEPT).append(COLON).append(SPACE).append("*/*").append(NEXT_LINE)
//				.append(HttpHeaderNames.ACCEPT_ENCODING).append(COLON).append(SPACE).append("gzip, deflate, sdch, br").append(NEXT_LINE)
//				.append(HttpHeaderNames.ACCEPT_LANGUAGE).append(COLON).append(SPACE).append("en-US,en;q=0.8")
//				.append(NEXT_LINE)
//				.toString();
		//@formatter:on
	}

	public static String createGetRequest(String host, String message) {
		//@formatter:off
		return HttpFirstLineBuilder.Build(METHOD_GET).add(message).add(HTTP_VERSION)
				.build().concat(createResponseHeader(host));
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
