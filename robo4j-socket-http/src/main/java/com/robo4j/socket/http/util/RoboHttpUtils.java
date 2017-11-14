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
import com.robo4j.socket.http.HttpHeaderFieldValues;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.enums.StatusCode;
import com.robo4j.socket.http.request.RoboBasicMapEntry;
import com.robo4j.socket.http.units.Constants;
import com.robo4j.util.StringConstants;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.robo4j.socket.http.HttpHeaderFieldValues.CONNECTION_KEEP_ALIVE;

/**
 * Basic Http constants and utils methods
 *
 * @author Miroslav Wengner (@miragemiko)
 */
public final class RoboHttpUtils {

    public static final String HTTP_VERSION = "HTTP/1.1";
    private static final String ROBO4J_CLIENT = "Robo4J-HttpClient";
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


    public static String createResponseWithHeaderAndMessage(String header, String message) {
        return header.concat(NEW_LINE_UNIX).concat(message);
    }

    public static String createResponseByCode(StatusCode code) {
        return RoboHttpUtils.createResponseWithHeaderAndMessage(
                RoboResponseHeader.headerByCode(code),
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
                .add(HttpHeaderFieldNames.HOST, host).add(HttpHeaderFieldNames.CONNECTION, CONNECTION_KEEP_ALIVE)
                .add(HttpHeaderFieldNames.CACHE_CONTROL, HttpHeaderFieldValues.NO_CACHE).add(HttpHeaderFieldNames.USER_AGENT, ROBO4J_CLIENT)
                .add(HttpHeaderFieldNames.ACCEPT, "*/*")
                .add(HttpHeaderFieldNames.ACCEPT_ENCODING, "gzip, deflate, sdch, br")
                .add(HttpHeaderFieldNames.ACCEPT_LANGUAGE, "en-US,en;q=0.8")
		        .add(HttpHeaderFieldNames.CONTENT_TYPE, "text/html;".concat(Constants.UTF8_SPACE).concat("charset=utf-8"));
        if(length != 0){
			builder.add(HttpHeaderFieldNames.CONTENT_LENGTH, String.valueOf(length));
		}
        return builder.build(method, HttpVersion.HTTP_1_1);
		//@formatter:on
    }

    public static String createRequest(HttpMethod method, String host, String path, String message) {
        final String header = createHeader(method, host, path, message.length());
        return createRequest(header, message);
    }

    public static String createRequest(String header, String message) {
        //@formatter:off
		return header
                .concat(NEW_LINE_MAC)
				.concat(NEW_LINE_UNIX)
				.concat(message);
		//@formatter:on
    }

    public static String createHeader(HttpMethod method, String host, String path, int length) {
        return createRequestHeader(method, host, path, length);
    }

    public static String createGetRequest(String host, String path) {
        return createRequestHeader(HttpMethod.GET, host, path, 0);
    }

    public static String correctLine(String line) {
        return line == null ? StringConstants.EMPTY : line;
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
