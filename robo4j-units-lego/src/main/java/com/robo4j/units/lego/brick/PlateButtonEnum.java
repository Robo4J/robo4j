/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This PlateButtonEnum.java  is part of robo4j.
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

package com.robo4j.units.lego.brick;

import com.robo4j.hw.lego.ILegoHardware;
import com.robo4j.units.lego.platform.LegoPlatformMessageType;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 31.01.2017
 */
public enum  PlateButtonEnum implements ILegoHardware<Integer> {

    //@formatter:off
    UP              (0, "up", LegoPlatformMessageType.MOVE),
    ENTER           (1, "enter", LegoPlatformMessageType.STOP),
    DOWN            (2, "down", LegoPlatformMessageType.BACK),
    RIGHT           (3, "right", LegoPlatformMessageType.RIGHT),
    LEFT            (4, "left", LegoPlatformMessageType.LEFT),
    ESCAPE          (5, "escape", null),
    ;

    //@formatter:on
    private volatile static Map<Integer, PlateButtonEnum> internMapByType;

    private int type;
    private String name;
    private LegoPlatformMessageType message;

    PlateButtonEnum(int type, String name, LegoPlatformMessageType message) {
        this.type = type;
        this.name = name;
        this.message = message;
    }

    public static PlateButtonEnum getByType(Integer type) {
        if (internMapByType == null) {
            internMapByType = initMapping();
        }
        return internMapByType.get(type);
    }

    public static PlateButtonEnum getByName(String name) {
        if (internMapByType == null) {
            internMapByType = initMapping();
        }
        //@formatter:off
        return internMapByType.entrySet().stream()
                .map(Map.Entry::getValue)
                .filter(b -> b.getName().equals(name))
                .findFirst()
                .get();
        //@formatter:on
    }

    public static Set<String> getButtonNames(){
        if(internMapByType == null){
            internMapByType = initMapping();
        }
        //@formatter:off
        return internMapByType.entrySet().stream()
                .map(Map.Entry::getValue)
                .map(PlateButtonEnum::getName)
                .collect(Collectors.toSet());
        //@formatter:on
    }


    @Override
    public Integer getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    public LegoPlatformMessageType getMessage() {
        return message;
    }

    // Private Methods
    private static Map<Integer, PlateButtonEnum> initMapping() {
        return Stream.of(values()).collect(Collectors.toMap(PlateButtonEnum::getType, e -> e));
    }

    @Override
    public String toString() {
        return "PlateButtonEnum{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
