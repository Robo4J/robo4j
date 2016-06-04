/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LegoEngineEnum.java is part of robo4j.
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
public enum  LegoEngineEnum implements LegoSystemEnum<Character>, RoboSystemConfig {

    //@formatter:off
    //          Type    desc
    NXT        ('N',    "NXTRegulatedMotor"),
    LARGE      ('L',    "EV3LargeRegulatedMotor"),
    MEDIUM     ('M',    "EV3MediumRegulatedMotor"),
    ;
    //@formatter:on

    private char type;
    private String desc;

    LegoEngineEnum(char type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    @Override
    public Character getType() {
        return type;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return "LegoEngineEnum{" +
                "type=" + type +
                ", desc='" + desc + '\'' +
                '}';
    }
}
