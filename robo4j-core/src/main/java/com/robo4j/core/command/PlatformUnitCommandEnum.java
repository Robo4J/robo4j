/*
 * Copyright (C) 2017. Miroslav Wengner, Marcus Hirt
 * This RequestCommandEnum.java  is part of robo4j.
 * module: robo4j-commons
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

package com.robo4j.core.command;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.robo4j.core.enums.RoboHardwareEnumI;
import com.robo4j.core.enums.RoboTargetEnumI;

/**
 * Command types designed for Robo-Brick client
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 09.06.2016
 */

//TODO: FIXME -> needs to be changed simplified duplication
public enum PlatformUnitCommandEnum implements RoboUnitCommand, RoboHardwareEnumI<CommandTypeEnum>, RoboTargetEnumI<CommandTargetEnum> {

	// @formatter:off
	EXIT		(0,		"exit"),
	INIT		(1,  	"init"),
	MOVE		(2,  	"move"),
	RIGHT		(3,  	"right"),
	LEFT		(4, 	"left"),
	BACK		(5, 	"back"),
	STOP		(6, 	"stop"),
	;
	// @formatter:on

	private volatile static Map<Integer, PlatformUnitCommandEnum> codeToCommandCodeMapping;

	private int code;
	private String name;

	PlatformUnitCommandEnum(int c, String name) {
		this.code = c;
		this.name = name;
	}

	public static PlatformUnitCommandEnum getCommand(String name) {
		if (codeToCommandCodeMapping == null) {
			codeToCommandCodeMapping = initMapping();
		}
		return codeToCommandCodeMapping.entrySet().stream().filter(e -> e.getValue().getName().equals(name))
				.map(Map.Entry::getValue).reduce(null, (e1, e2) -> e2);
	}


	public int getCode() {
		return code;
	}

	@Override
	public CommandTypeEnum getType() {
		return CommandTypeEnum.DIRECT;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public CommandTargetEnum getTarget() {
		return CommandTargetEnum.PLATFORM;
	}

	@Override
	public String toString() {
		return "RequestCommandEnum{" + "code=" + code + ", target=" + getTarget() + ", name='" + name + '\'' + '}';
	}

	// Private Methods
	private static Map<Integer, PlatformUnitCommandEnum> initMapping() {
		return Arrays.stream(values()).collect(Collectors.toMap(PlatformUnitCommandEnum::getCode, e -> e));
	}
}
