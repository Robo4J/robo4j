/*
 * Copyright (C)  2016. Miroslav Wengner and Marcus Hirt
 * This LegoEnginePartEnum.java  is part of robo4j.
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
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 05.05.2016
 */
public enum LegoEnginePartEnum implements RoboHardwareEnum<String>, RoboSystemConfig {

	// @formatter:off
	NONE("NONE", "not assigned"), PLATFORM("PLATFORM", "Platform Engine to control direction"), HAND("HAND",
			"Engine controls Lego Hand"),;
	// @formatter:on

	private String type;
	private String name;

	LegoEnginePartEnum(String type, String name) {
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
		return "LegoEnginePartEnum{" + "type='" + type + '\'' + ", name='" + name + '\'' + '}';
	}
}
