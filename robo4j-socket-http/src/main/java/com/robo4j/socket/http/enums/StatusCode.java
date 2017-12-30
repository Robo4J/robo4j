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
package com.robo4j.socket.http.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum StatusCode {

    //@formatter:off
    OK                  (200, "OK"),
    ACCEPTED            (202, "Accepted"),
    BAD_REQUEST         (400, "Bad Request"),
    NOT_FOUND           (404, "Not Found"),
    NOT_ACCEPTABLE      (406, "Not Acceptable"),
    NOT_IMPLEMENTED     (501, "Not Implemented");
    //@formatter:on

    private static Map<Integer, StatusCode> toCodeMap;
    private int code;
    private String reasonPhrase;

    StatusCode(int code, String reasonPhrase) {
        this.code = code;
        this.reasonPhrase = reasonPhrase;
    }

    private static Map<Integer, StatusCode> initMapping() {
        return Arrays.stream(values()).collect(Collectors.toMap(StatusCode::getCode, e -> e));
    }

    public static StatusCode getByCode(Integer name) {
        if (toCodeMap == null) {
            toCodeMap = initMapping();
        }
        return toCodeMap.get(name);
    }

    public int getCode() {
        return code;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }


}
