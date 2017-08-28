/*
 * Copyright (C) 2016. Miroslav Wengner, Marcus Hirt
 * This RequestType.java is part of robo4j.
 *
 *     robo4j is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     robo4j is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.socket.http.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum RequestType {

	// @formatter:off
	NONE	(0, "none"),
	HTTP	(1, "http"),
	RAW		(2, "raw"),
	;
	private volatile static Map<String, RequestType> defNameToTypeMapping;
	// @formatter:on
	private int code;
	private String name;

	RequestType(int code, String name) {
		this.code = code;
		this.name = name;
	}

	// Private Methods
	private static Map<String, RequestType> initMapping() {
		return Arrays.stream(values()).collect(Collectors.toMap(RequestType::getName, e -> e));
	}

	public static RequestType getByName(String name) {
		if (defNameToTypeMapping == null) {
			defNameToTypeMapping = initMapping();
		}
		return defNameToTypeMapping.get(name);
	}

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "RequestType{" + "code=" + code + ", name='" + name + '\'' + '}';
	}
}
