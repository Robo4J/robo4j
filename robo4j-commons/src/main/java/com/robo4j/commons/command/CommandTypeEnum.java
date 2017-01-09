/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This CommandTypeEnum.java  is part of robo4j.
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

import com.robo4j.commons.enums.RoboHardwareEnum;
import com.robo4j.commons.util.CommandUtil;

/**
 * Available command types accepted by Robot
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 24.06.2016
 */
public enum CommandTypeEnum implements RoboHardwareEnum<Integer> {

	// @formatter:off
	BATCH(0, "B".concat(CommandUtil.getElementClose())), DIRECT(1, "D".concat(CommandUtil.getElementClose())), HAND(2,
			"H".concat(CommandUtil.getElementClose())), COMPLEX(3,
					"C".concat(CommandUtil.getElementClose())), ACTIVE(4, "A".concat(CommandUtil.getElementClose())),;
	// @formatter:on

	private volatile static Map<String, CommandTypeEnum> defToCommandTypeMapping;
	private int code;
	private String name;

	CommandTypeEnum(int code, String name) {
		this.code = code;
		this.name = name;
	}

	private static void initMapping() {
		defToCommandTypeMapping = new HashMap<>();
		for (CommandTypeEnum ct : values()) {
			defToCommandTypeMapping.put(ct.getName(), ct);
		}
	}

	public static CommandTypeEnum getByDefinition(String def) {
		if (defToCommandTypeMapping == null)
			initMapping();
		return defToCommandTypeMapping.get(def);
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
		return "CommandTypeEnum{" + "code=" + code + ", name='" + name + '\'' + '}';
	}
}
