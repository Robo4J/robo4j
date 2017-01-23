/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This FrontHandCommandEnum.java  is part of robo4j.
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

package com.robo4j.core.command;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.robo4j.core.enums.RoboHardwareEnumI;
import com.robo4j.core.enums.RoboTargetEnumI;

/**
 * @author Miroslav Wengner (@miragemiko)
 * @since 27.04.2016
 */

public enum HandUnitCommandEnum implements RoboUnitCommand, RoboHardwareEnumI<CommandTypeEnum>, RoboTargetEnumI<CommandTargetEnum> {

	// @formatter:off
	EXIT		(0, "exit"),
	HAND_OPEN	(1, "hand_open"),
	HAND_CLOSE 	(2, "hand_close"),
	;
	// @formatter:on

	private volatile static Map<Integer, HandUnitCommandEnum> codeToCommandCodeMapping;
	private int code;
	private String name;

	HandUnitCommandEnum(int c, String name) {
		this.code = c;
		this.name = name;
	}


	public static HandUnitCommandEnum getCommand(int code) {
		init();
		return codeToCommandCodeMapping.entrySet().stream().filter(e -> e.getValue().getCode() == code)
				.map(Map.Entry::getValue).reduce(null, (e1, e2) -> e2);
	}

	public static HandUnitCommandEnum getCommand(String name) {
		init();
		return codeToCommandCodeMapping.entrySet().stream().filter(e -> e.getValue().getName().equals(name))
				.map(Map.Entry::getValue).reduce(null, (e1, e2) -> e2);
	}

	public int getCode() {
		return code;
	}


	@Override
	public String getName() {
		return name;
	}

	@Override
	public CommandTargetEnum getTarget() {
		return CommandTargetEnum.HAND_UNIT;
	}

	@Override
	public CommandTypeEnum getType() {
		return CommandTypeEnum.HAND;
	}

	//Private Methods
	//TODO: can be moved into the stream
	private static void init(){
		if (codeToCommandCodeMapping == null) {
			codeToCommandCodeMapping = initMapping();
		}
	}

	private static Map<Integer, HandUnitCommandEnum> initMapping() {
		return Arrays.stream(values()).collect(Collectors.toMap(HandUnitCommandEnum::getCode, e -> e));
	}


}
