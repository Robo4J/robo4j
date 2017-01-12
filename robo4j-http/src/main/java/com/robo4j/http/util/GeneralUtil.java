/*
 * Copyright (C) 2016. Miroslav Wengner, Marcus Hirt
 * This GenaralUtil.java is part of robo4j.
 *
 *     robo4j is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     robo4j is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.http.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.robo4j.http.HttpMessage;
import com.robo4j.http.HttpMethod;
import com.robo4j.http.HttpVersion;

/**
 * @author Miro Wengner (@miragemiko)
 * @since 23.10.2016
 */
public class GeneralUtil {

	public final static String HTTP_EMPTY_SEP = "\\s+";
	private static final int METHOD_KEY_POSITION = 0, URI_VALUE_POSITION = 1, VERSION_POSITION = 2, HTTP_HEADER_SEP = 9;
	private final static String HTTP_SEPARATORS = " ,;=()<>@:\\\"/[]?{}\t";
	private static final String SPACE = "\u0020";
	private static final String NEXT_LINE = "\r\n";
	private static final String STRING_EMPTY = "";

	public static String setHeader(String server, String responseCode, int length) throws IOException {
		return new StringBuilder(STRING_EMPTY).append(responseCode).append(NEXT_LINE).append("Date:").append(SPACE)
				.append(LocalDateTime.now()).append(NEXT_LINE).append("Server:").append(SPACE).append(server)
				.append(NEXT_LINE).append("Content-length:").append(SPACE).append(length).append(NEXT_LINE)
				.append("Content-type: text/html; charset=utf-8").append(NEXT_LINE).append(NEXT_LINE).toString();
	}

	public static String correctLine(String line) {
		return line == null ? "" : line;
	}

	public static String getHttpSeparator(int position) {
		return Character.toString(HTTP_SEPARATORS.charAt(position));
	}

	@SuppressWarnings(value = "unchecked")
	public static HttpMessage getHttpMessage(BufferedReader in) throws IOException {
		final Map<String, String> tmpMap = new ConcurrentHashMap<>();
		boolean firstLine = true;
		String[] tokens = null;
		String method = null;
		String inputLine;

		while (!(inputLine = GeneralUtil.correctLine(in.readLine())).equals(STRING_EMPTY)) {
			if (firstLine) {
				tokens = inputLine.split(HTTP_EMPTY_SEP);
				method = tokens[METHOD_KEY_POSITION];
				firstLine = false;
			} else {
				final String[] array = inputLine.split(getHttpSeparator(HTTP_HEADER_SEP));
				tmpMap.put(array[METHOD_KEY_POSITION], array[URI_VALUE_POSITION]);
			}
		}
		final HttpMethod httpMethod = HttpMethod.getByName(method);

		return Objects.nonNull(httpMethod) && Objects.nonNull(tokens)
				? new HttpMessage(httpMethod, URI.create(tokens[URI_VALUE_POSITION]),
						HttpVersion.getByValue(tokens[VERSION_POSITION]), tmpMap)
				: new HttpMessage(HttpMethod.GET, null, HttpVersion.HTTP_1_0, Collections.EMPTY_MAP);
	}

}
