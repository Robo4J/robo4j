/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This LegoSensorPortEnum.java  is part of robo4j.
 *
 *  robo4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  robo4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.lego.enums;

import com.robo4j.commons.control.RoboSystemConfig;
import com.robo4j.commons.enums.RoboHardwareEnum;

/**
 * Configuration interface
 *
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 19.06.2016
 */
public enum LegoSensorPortEnum implements RoboHardwareEnum<String, LegoSensorPortEnum>, RoboSystemConfig {

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

	LegoSensorPortEnum(String type, String name) {
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
	public LegoSensorPortEnum getTarget() {
		return null;
	}


	@Override
	public String toString() {
		return "LegoSensorPortEnum{" + "type='" + type + '\'' + ", name='" + name + '\'' + '}';
	}
}
