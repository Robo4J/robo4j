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
package com.robo4j.hw.lego.enums;

import com.robo4j.hw.lego.ILegoHardware;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Available Lego Motors
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum MotorTypeEnum implements ILegoHardware<Character> {

	// @formatter:off
	// Type name
	NXT			('N', "NXTRegulatedMotor"),
	LARGE		('L', "EV3LargeRegulatedMotor"),
	MEDIUM		('M', "EV3MediumRegulatedMotor"),
	;
	// @formatter:on

	private volatile static Map<Character, MotorTypeEnum> internMapByType;
	private char type;
	private String name;

	MotorTypeEnum(char type, String name) {
		this.type = type;
		this.name = name;
	}

	public static MotorTypeEnum getByType(Character type) {
		if (internMapByType == null) {
			internMapByType = initMapping();
		}
		return internMapByType.get(type);
	}

	@Override
	public Character getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	// Private Methods
	private static Map<Character, MotorTypeEnum> initMapping() {
		return Stream.of(values()).collect(Collectors.toMap(MotorTypeEnum::getType, e -> e));
	}


	@Override
	public String toString() {
		return "MotorTypeEnum{" +
				"type=" + type +
				", name='" + name + '\'' +
				'}';
	}
}
