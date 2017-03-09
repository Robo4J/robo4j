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

package com.robo4j.http.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class HttpHeaderBuilder {
    private static final String STRING_EMPTY = "";
    private final Map<String, String> map;

    private HttpHeaderBuilder(){
        map = new LinkedHashMap<>();
    }

    public static HttpHeaderBuilder Build(){
        return new HttpHeaderBuilder();
    }

    public HttpHeaderBuilder add(String key, String value){
        map.put(key, value);
        return this;
    }


    public String build(){
        return map.entrySet().stream()
                .map(e -> e.getKey().concat(HttpMessageUtil.COLON)
                        .concat(HttpMessageUtil.SPACE)
                        .concat(e.getValue())
                        .concat(HttpMessageUtil.NEXT_LINE))
                .collect(Collectors.joining(STRING_EMPTY));
    }
}
