/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This LegoSensorPortEnum.java  is part of robo4j.
 * module: robo4j-hw-lego
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

package com.robo4j.hw.lego.enums;

import com.robo4j.hw.lego.ILegoHardware;

/**
 * Configuration interface
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 19.06.2016
 */
public enum DigitalPortEnum implements ILegoHardware<String> {

	// @formatter:off
	// type name
	S1		("S1", "Sensor S1"),
	S2		("S2", "Sensor S2"),
	S3		("S3", "Sensor S3"),
	S4		("S4", "Sensor S4"),
	;
	// @formatter:on

	private String type;
	private String name;

	DigitalPortEnum(String type, String name) {
		this.type = type;
		this.name = name;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "LegoDigitalPortEnum{" + "type='" + type + '\'' + ", name='" + name + '\'' + '}';
	}
}
