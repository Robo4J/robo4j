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

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.util.StringConstants;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.robo4j.socket.http.util.HttpConstant.HTTP_NEW_LINE;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class HttpHeaderBuilder {
	private final Map<String, String> map = new LinkedHashMap<>();
	private HttpFirstLineBuilder firstLineBuilder;

	private HttpHeaderBuilder() {

	}

	public static HttpHeaderBuilder Build() {
		return new HttpHeaderBuilder();
	}

	public Map<String, String> getMap() {
		return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	public String getValue(String key) {
		return String.valueOf(map.get(key));
	}

	public HttpHeaderBuilder add(String key, String value) {
		map.put(key, value);
		return this;
	}

	public HttpHeaderBuilder addAll(Map<String, String> map) {
		this.map.putAll(map);
		return this;
	}

	/**
	 * Helps to build 1st line of Http Request/Response Header
	 *
	 * @param value
	 *            1st available value in the string
	 * @return http header builder
	 */
	public HttpHeaderBuilder addFirstLine(Object value) {
		if (firstLineBuilder == null) {
			firstLineBuilder = HttpFirstLineBuilder.Build(value);
		} else {
			firstLineBuilder.add(value);
		}
		return this;
	}

	public String build() {
		final String start = firstLineBuilder == null || firstLineBuilder.isEmpty() ? StringConstants.EMPTY
				: firstLineBuilder.build().concat(HTTP_NEW_LINE);
		return start.concat(map
				.entrySet().stream().map(e -> e.getKey().concat(HttpMessageUtils.COLON).concat(HttpMessageUtils.SPACE)
						.concat(e.getValue()).concat(HTTP_NEW_LINE))
				.collect(Collectors.joining(StringConstants.EMPTY)));
	}

	/**
	 * 
	 * @param method
	 *            desired Http Method
	 * @param version
	 *            Http Version
	 * @return String
	 */
	public String build(HttpMethod method, HttpVersion version) {
		addFirstLine(version.getValue());
		//@formatter:off
        return method.getName()
                .concat(HttpMessageUtils.SPACE)
                .concat(firstLineBuilder.build())
                .concat(HTTP_NEW_LINE)
                .concat(map.entrySet().stream()
                        .map(e -> e.getKey().concat(HttpMessageUtils.COLON)
                                .concat(HttpMessageUtils.SPACE)
                                .concat(e.getValue())
                                .concat(HTTP_NEW_LINE))
                        .collect(Collectors.joining(StringConstants.EMPTY)));
        //@formatter:on
	}

}
