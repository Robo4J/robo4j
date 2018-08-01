/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This LegoPlatformMessageTypeEnum.java  is part of robo4j.
 * module: robo4j-units-lego
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.units.lego.enums;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * LegoMindstorm available buttons
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum LegoPlatformMessageTypeEnum implements LegoEnum {

	//@formatter:off
    STOP        (0, "stop"),
    MOVE        (1, "move"),
    BACK        (2, "back"),
    LEFT        (3, "left"),
    RIGHT       (4, "right"),
	SPEED		(5, "speed")
    ;
    //@formatter:on

	private static volatile Map<Integer, LegoPlatformMessageTypeEnum> internMapByType;
	private int type;
	private String name;

	LegoPlatformMessageTypeEnum(int type, String name) {
		this.type = type;
		this.name = name;
	}

	//@formatter:off
    private static Map<Integer, LegoPlatformMessageTypeEnum> initMapping() {
        return Stream.of(values())
                .collect(Collectors.toMap(LegoPlatformMessageTypeEnum::getType, e -> e));
    }
    
    public static LegoPlatformMessageTypeEnum getByName(String name) {
        initiate();
        return internMapByType.entrySet().stream()
                .map(Map.Entry::getValue)
                .filter(e -> e.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public static LegoPlatformMessageTypeEnum getById(int id){
		initiate();
		return internMapByType.get(id);
	}

	private static void initiate() {
    	if (internMapByType == null)
			internMapByType = initMapping();
	}
    //@formatter:on

	@Override
	public int getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "LegoPlatformMessageType{" + "type=" + type + ", name='" + name + '\'' + '}';
	}

}
