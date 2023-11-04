/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
 * Analog Lego Brick
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum AnalogPortEnum implements ILegoHardware<String> {

	// @formatter:off
	A		("A", "Analog A"),
	B		("B", "Analog B"),
	C		("C", "Analog C"),
	D		("D", "Analog D"),
	;
	// @formatter:on

	private static final Map<String, AnalogPortEnum> internMapByType = initMapping();
	private final String type;
	private final String name;

	/**
	 *
	 * @param type - define Analog Lego MindStorm port
	 * @param name - description
     */
	AnalogPortEnum(String type, String name) {
		this.type = type;
		this.name = name;
	}

	public static AnalogPortEnum getByType(String type) {
		return internMapByType.get(type);
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	// Private Methods
	private static Map<String, AnalogPortEnum> initMapping() {
		return Stream.of(values()).collect(Collectors.toMap(AnalogPortEnum::getType, e -> e));
	}


	@Override
	public String toString() {
		return "AnalogPortEnum{" + "type='" + type + '\'' + ", name='" + name + '\'' + '}';
	}
}
