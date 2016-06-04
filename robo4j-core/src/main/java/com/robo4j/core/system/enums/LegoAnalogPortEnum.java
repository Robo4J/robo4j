/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LegoAnalogPortEnum.java is part of robo4j.
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

package com.robo4j.core.system.enums;

import com.robo4j.core.control.RoboSystemConfig;
import com.robo4j.core.system.LegoSystemEnum;

/**
 * Created by miroslavkopecky on 04/05/16.
 */
public enum  LegoAnalogPortEnum implements LegoSystemEnum<String>, RoboSystemConfig {

    //@formatter:off
    A ("A", "Analog Bug A"),
    B ("B", "Analog Bug B"),
    C ("C", "Analog Bug C"),
    D ("D", "Analog Bug D"),
    ;
    //@formatter:on

    private String type;
    private String description;

    LegoAnalogPortEnum(String type, String description) {
        this.type = type;
        this.description = description;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getDesc() {
        return description;
    }

    @Override
    public String toString() {
        return "LegoAnalogPortEnum{" +
                "type='" + type + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
