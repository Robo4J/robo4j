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
 * VideoMessageType {@link com.robo4j.socket.http.codec.VideoConfigMessage}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum VideoMessageType {
    // @formatter:off
	START	    (0, "start"),
	STOP	    (1, "stop"),
	CONFIG		(2, "config"),
	;
	private static Map<String, VideoMessageType> defNameToTypeMapping;
	// @formatter:on
    private int code;
    private String name;

    VideoMessageType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    private static Map<String, VideoMessageType> initMapping() {
        return Arrays.stream(values()).collect(Collectors.toMap(VideoMessageType::getName, e -> e));
    }

    public static VideoMessageType getByName(String name) {
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
}
