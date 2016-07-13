/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This RoboExceptionEnum.java is part of robo4j.
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

package com.robo4j.core.util;

import com.robo4j.commons.enums.LegoSystemEnum;

/**
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 23.05.2016
 */
public enum  RoboExceptionEnum implements LegoSystemEnum<Integer> {

    //@formatter:off
    RESOURCES_NOT   (0, "Resource not available"),
    RESOURCES_READ  (1, "Resource already read"),
    HTTP_SERVER     (2, "Server Problem"),
    ;
    //@formatter:on

    private int code;
    private String name;

    RoboExceptionEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getType() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String toString() {
        return "RoboExceptionEnum{" +
                "code=" + code +
                ", name='" + name + '\'' +
                '}';
    }
}
