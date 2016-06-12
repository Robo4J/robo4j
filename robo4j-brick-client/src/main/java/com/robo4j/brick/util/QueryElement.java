/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This QueryElement.java is part of robo4j.
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
 *
 */

package com.robo4j.brick.util;

import com.robo4j.brick.client.enums.RequestCommandEnum;

/**
 * Created by miroslavkopecky on 09/06/16.
 */
public class QueryElement {
    private RequestCommandEnum name;
    private String value;

    public QueryElement(String value) {
        String[] values = value.split(ConstantUtil.getHttpSeparator(3));
        this.name = RequestCommandEnum.getRequestValue(values[ConstantUtil.DEFAULT_VALUE]);
        this.value = values[1];
    }

    public RequestCommandEnum getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "QueryElement{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
