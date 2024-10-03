/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enum is used to distinguished the movement direction motor direction of
 * rotation
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum MotorRotationEnum implements LegoEnum {

	//@formatter:off
	FORWARD			(0, 1, "forward"),
	STOP			(1, 0, "stop"),
	BACKWARD		(2, 2, "backward"),;
	// @formatter:on

	private static volatile Map<Integer, MotorRotationEnum> internMapByType;
	private int type;
	private int code;
	private String name;

	MotorRotationEnum(int type, int code, String name) {
		this.type = type;
		this.code = code;
		this.name = name;
	}

	//@formatter:off
    private static Map<Integer, MotorRotationEnum> initMapping() {
        return Stream.of(values())
                .collect(Collectors.toMap(MotorRotationEnum::getType, e -> e));
    }

    public static MotorRotationEnum getByName(String name) {
        if (internMapByType == null)
            internMapByType = initMapping();
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

	public int getCode() {
		return code;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "MotorRotationEnum{" + "type=" + type + ", code=" + code + ", name='" + name + '\'' + '}';
	}
}
