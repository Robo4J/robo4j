/*
 * Copyright (C) 2016. Miroslav Wengner, Marcus Hirt
 * This HttpVersion.java is part of robo4j.
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

package com.robo4j.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Miro Wengner (@miragemiko)
 * @since 06.03.2016
 */
public enum HttpVersion {

	// @formatter:off
	HTTP_1_0("HTTP/1.0"), HTTP_1_1("HTTP/1.1");
	// @formatter:on

	private volatile static Map<String, HttpVersion> valueToHttpVersion;
	private String value;

	HttpVersion(String value) {
		this.value = value;
	}

	// Utils Method
	public static HttpVersion getByValue(String value) {
		if (Objects.isNull(valueToHttpVersion)) {
			iniMapping();
		}
		return valueToHttpVersion.get(value);
	}

	public static boolean containsValue(HttpVersion version) {
		return valueToHttpVersion.containsValue(version);
	}

	// Private Methods
	private static void iniMapping() {
		valueToHttpVersion = new HashMap<>();
		for (HttpVersion version : values()) {
			valueToHttpVersion.put(version.getValue(), version);
		}
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "HttpVersion{" + "value='" + value + '\'' + '}';
	}
}
