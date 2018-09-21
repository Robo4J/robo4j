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

package com.robo4j.socket.http.provider;

import com.robo4j.socket.http.HttpHeaderFieldNames;
import com.robo4j.socket.http.HttpHeaderFieldValues;
import com.robo4j.util.PropertyMapBuilder;

import java.util.Map;

import static com.robo4j.socket.http.HttpHeaderFieldValues.CONNECTION_KEEP_ALIVE;
import static com.robo4j.util.Utf8Constant.UTF8_SPACE;

/**
 * provides default values for http related structures construction
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class DefaultValuesProvider {
    public static final String ROBO4J_CLIENT = "Robo4J-HttpClient";

    public static final Map<String, String> BASIC_HEADER_MAP = new PropertyMapBuilder<String, String>()
            .put(HttpHeaderFieldNames.CACHE_CONTROL, HttpHeaderFieldValues.NO_CACHE)
            .put(HttpHeaderFieldNames.USER_AGENT, ROBO4J_CLIENT)
            .put(HttpHeaderFieldNames.CONNECTION, CONNECTION_KEEP_ALIVE)
            .put(HttpHeaderFieldNames.ACCEPT, "*/*")
            .put(HttpHeaderFieldNames.ACCEPT_ENCODING, "gzip, deflate, sdch, br")
            .put(HttpHeaderFieldNames.ACCEPT_LANGUAGE, "en-US,en;q=0.8")
            .put(HttpHeaderFieldNames.CONTENT_TYPE, "text/html;".concat(UTF8_SPACE).concat("charset=utf-8"))
            .create();
}
