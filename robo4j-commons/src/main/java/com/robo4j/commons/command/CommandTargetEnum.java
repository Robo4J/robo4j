/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This CommandTargetEnum.java  is part of robo4j.
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

package com.robo4j.commons.command;

import java.util.HashMap;
import java.util.Map;

import com.robo4j.commons.enums.RoboHardwareEnumI;

/**
 * Command Target helps with particular command destination
 *
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 05.07.2016
 */
public enum CommandTargetEnum implements RoboHardwareEnumI<Integer> {

	// @formatter:off
	SYSTEM			(0, "system"),
	PLATFORM		(1, "platform"),
	HAND_UNIT		(2, "hand_unit"),
	FRONT_UNIT		(3, "front_unit"),
	LCD_UNIT		(4, "lcd_unit"),
	;
	// @formatter:on

	private volatile static Map<String, CommandTargetEnum> defToCommandTargetMapping;
	private int code;
	private String name;

	CommandTargetEnum(int code, String name) {
		this.code = code;
		this.name = name;
	}

	private static void initMapping() {
		defToCommandTargetMapping = new HashMap<>();
		for (CommandTargetEnum ct : values()) {
			defToCommandTargetMapping.put(ct.getName(), ct);
		}
	}

	public static CommandTargetEnum getByName(String def) {
		if (defToCommandTargetMapping == null)
			initMapping();
		return defToCommandTargetMapping.get(def);
	}

	@Override
	public Integer getType() {
		return code;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "CommandTargetEnum{" + "code=" + code + ", name='" + name + '\'' + '}';
	}
}
