/*
 * Copyright (c) 2014-2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum SystemPath {

	//@formatter:off
    NONE                   (""),
    UNITS                  ("units"),
    ;
    //@formatter:on

	private static Map<String, SystemPath> toPathMap;
	private String path;

	SystemPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public static SystemPath getByPath(String name) {
		if (toPathMap == null) {
			toPathMap = initMapping();
		}
		return toPathMap.get(name);
	}

	private static Map<String, SystemPath> initMapping() {
		return Arrays.stream(values()).collect(Collectors.toMap(SystemPath::getPath, e -> e));
	}

}
