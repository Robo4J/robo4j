/*
 * Copyright (c) 2014-2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.units.lego.enums;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum LegoSonicMessageTypeEnum implements LegoEnum {

	//@formatter:off
    FINISH      (0, "finish"),
    SCAN        (1, "scan"),
    CENTER      (2, "center"),
    STOP        (3, "stop"),
    ;


    //@formatter:on

	private static volatile Map<Integer, LegoSonicMessageTypeEnum> internMapByType;
	private int type;
	private String name;

	LegoSonicMessageTypeEnum(int type, String name) {
		this.type = type;
		this.name = name;
	}

	//@formatter:off
    private static Map<Integer, LegoSonicMessageTypeEnum> initMapping() {
        return Stream.of(values())
                .collect(Collectors.toMap(LegoSonicMessageTypeEnum::getType, e -> e));
    }
    public static LegoSonicMessageTypeEnum getInternalByName(String name) {
        if (internMapByType == null) {
            internMapByType = initMapping();
        }
        return internMapByType.entrySet().stream()
                .map(Map.Entry::getValue)
                .filter(e -> e.getName().equals(name))
                .findFirst()
                .orElse(null);
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

	public Set<String> commandNames() {
		//@formatter:off
        return Stream.of(values())
                .map(LegoSonicMessageTypeEnum::getName)
                .collect(Collectors.toSet());
        //@formatter:on
	}

	public LegoSonicMessageTypeEnum getByName(String name) {
		return getInternalByName(name);
	}

	@Override
	public String toString() {
		return "LegoSonicMessageTypeEnum{" + "type=" + type + ", name='" + name + '\'' + '}';
	}
}
