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

import com.robo4j.socket.http.HttpHeaderFieldNames;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.enums.StatusCode;
import com.robo4j.util.StringConstants;

import static com.robo4j.socket.http.provider.DefaultValuesProvider.basicHeaderMap;

/**
 * Basic Http constants and utils methods
 *
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
	public static final String HTTP_TARGET_UNITS = "targetUnits";
	public static final String HTTP_PROPERTY_PORT = "port";
	public static final String HTTP_PROPERTY_BUFFER_CAPACITY = "bufferCapacity";


	public static String createResponseWithHeaderAndMessage(String header, String message) {
		String result = header.concat(NEW_LINE_MAC).concat(NEW_LINE_UNIX);
		return  message.isEmpty() ? result : result.concat(message);
	}

	public static String createResponseByCode(StatusCode code) {
		return RoboHttpUtils.createResponseWithHeaderAndMessage(RoboResponseHeader.headerByCode(code),
				StringConstants.EMPTY);
	}

	/**
	 * creat Default request Header
	 *
	 * @param host
	 * @param length
	 * @return
	 */
	public static String createRequestHeader(HttpMethod method, String host, String path, int length) {
		//@formatter:off

		HttpHeaderBuilder builder =  HttpHeaderBuilder.Build()
                .addFirstLine(path)
                .add(HttpHeaderFieldNames.HOST, host)
                .addAll(basicHeaderMap);
        if(length != 0){
			builder.add(HttpHeaderFieldNames.CONTENT_LENGTH, String.valueOf(length));
		}
        return builder.build(method, HttpVersion.HTTP_1_1);
		//@formatter:on
	}

	public static String createRequest(HttpMethod method, String host, String path, String message) {
		final String header = createRequestHeader(method, host, path, message.length());
		return createResponseWithHeaderAndMessage(header, message);
	}

	public static String correctLine(String line) {
		return line == null ? StringConstants.EMPTY : line;
	}

}
