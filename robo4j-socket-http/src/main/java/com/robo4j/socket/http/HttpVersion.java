/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum HttpVersion {
	// @formatter:off
	HTTP_1_0(Constants.VALUE_HTTP_1_0),
	HTTP_1_1(Constants.VALUE_HTTP_1_1);
	// @formatter:on

	private String value;

	HttpVersion(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "HttpVersion{" + "value='" + value + '\'' + '}';
	}

	public static HttpVersion getByValue(String string) {
		switch (string) {
		case Constants.VALUE_HTTP_1_0:
			return HTTP_1_0;
		case Constants.VALUE_HTTP_1_1:
			return HTTP_1_1;
		default:
			return null;
		}
	}
	
	private static class Constants {
		private final static String VALUE_HTTP_1_0 = "HTTP/1.0";
		private final static String VALUE_HTTP_1_1 = "HTTP/1.1";
	}
}
