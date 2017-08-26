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
package com.robo4j.socket.http.request;

import com.robo4j.socket.http.units.Constants;
import com.robo4j.socket.http.util.HttpMessageUtil;

/**
 * @author Miro Wengner (@miragemiko)
 */
public class RoboBasicMapEntry {

    private String key;
    private String value;

    public RoboBasicMapEntry(String text) {
        final String[] values = text.split(HttpMessageUtil.getHttpSeparator(3));
        key = values[Constants.DEFAULT_VALUE_0];
        value = values[Constants.DEFAULT_VALUE_1];
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "RoboBasicMapEntry{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
