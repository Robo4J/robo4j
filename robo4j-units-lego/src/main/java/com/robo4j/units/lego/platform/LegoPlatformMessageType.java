/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This LegoPlatformMessageType.java  is part of robo4j.
 * module: robo4j-units-lego
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.units.lego.platform;

import com.robo4j.core.enums.RoboHardwareEnumI;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 30.01.2017
 */
public enum LegoPlatformMessageType implements RoboHardwareEnumI<Integer> {

    //@formatter:off
    STOP        (0, "stop"),
    MOVE        (1, "move"),
    BACK        (2, "back"),
    LEFT        (3, "left"),
    RIGHT       (4, "right");


    //@formatter:on

    private volatile static Map<Integer, LegoPlatformMessageType> internMapByType;
    private Integer type;
    private String name;

    LegoPlatformMessageType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    //@formatter:off
    private static Map<Integer, LegoPlatformMessageType> initMapping() {
        return Stream.of(values())
                .collect(Collectors.toMap(LegoPlatformMessageType::getType, e -> e));
    }
    public static LegoPlatformMessageType getByText(String text) {
        if (internMapByType == null)
            internMapByType = initMapping();
        return internMapByType.entrySet().stream()
                .map(Map.Entry::getValue)
                .filter(e -> e.getName().equals(text))
                .findFirst().get();
    }
    //@formatter:on

    @Override
    public Integer getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "LegoPlatformMessageType{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
