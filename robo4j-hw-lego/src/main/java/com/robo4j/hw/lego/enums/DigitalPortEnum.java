/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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
 * Configuration interface
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum DigitalPortEnum implements ILegoHardware<String> {

	// @formatter:off
	// 		type    name
	S1		("S1", "Sensor S1"),
	S2		("S2", "Sensor S2"),
	S3		("S3", "Sensor S3"),
	S4		("S4", "Sensor S4"),
	;
	// @formatter:on

	private volatile static Map<String, DigitalPortEnum> internMapByType;
	private String type;
	private String name;

	DigitalPortEnum(String type, String name) {
		this.type = type;
		this.name = name;
	}

	public static DigitalPortEnum getByType(String type) {
		if (internMapByType == null) {
			internMapByType = initMapping();
		}
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
	private static Map<String, DigitalPortEnum> initMapping() {
		return Stream.of(values()).collect(Collectors.toMap(DigitalPortEnum::getType, e -> e));
	}


	@Override
	public String toString() {
		return "LegoDigitalPortEnum{" + "type='" + type + '\'' + ", name='" + name + '\'' + '}';
	}
}
