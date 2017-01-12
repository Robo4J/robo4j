/*
 * Copyright (C)  2016. Miroslav Wengner and Marcus Hirt
 * This RequestCommandEnum.java  is part of robo4j.
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

package com.robo4j.core.client.enums;

import static com.robo4j.commons.command.CommandTargetEnum.FRONT_UNIT;
import static com.robo4j.commons.command.CommandTargetEnum.HAND_UNIT;
import static com.robo4j.commons.command.CommandTargetEnum.PLATFORM;
import static com.robo4j.commons.command.CommandTargetEnum.SYSTEM;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.robo4j.commons.command.CommandTargetEnum;
import com.robo4j.commons.command.CommandTypeEnum;
import com.robo4j.commons.command.RoboUnitCommand;
import com.robo4j.commons.enums.RoboHardwareEnum;

/**
 * Command types designed for Robo-Brick client
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 09.06.2016
 */
public enum RequestCommandEnum implements RoboUnitCommand, RoboHardwareEnum<CommandTypeEnum> {

	// @formatter:off
	EXIT		(0, 	SYSTEM, "exit"),
	MOVE		(1, 	PLATFORM, "move"),
	RIGHT		(2, 	PLATFORM, "right"),
	LEFT		(3, 	PLATFORM, "left"),
	BACK		(4, 	PLATFORM, "back"),
	STOP		(5, 	PLATFORM, "stop"),
	HAND		(6, 	HAND_UNIT,"hand"),
	FRONT_LEFT	(7, FRONT_UNIT, "front_left"),
	FRONT_RIGHT	(8, FRONT_UNIT, "front_right"),
	;
	// @formatter:on

	private volatile static Map<Integer, RequestCommandEnum> codeToLegoCommandCodeMapping;

	private int code;
	private CommandTargetEnum target;
	private String name;

	RequestCommandEnum(int c, CommandTargetEnum target, String name) {
		this.code = c;
		this.target = target;
		this.name = name;
	}

	public static RequestCommandEnum getRequestValue(String name) {
		if (codeToLegoCommandCodeMapping == null) {
			codeToLegoCommandCodeMapping = initMapping();
		}
		return codeToLegoCommandCodeMapping.entrySet().stream().filter(e -> e.getValue().getName().equals(name))
				.map(Map.Entry::getValue).reduce(null, (e1, e2) -> e2);
	}

	public static RequestCommandEnum getRequestCommand(CommandTargetEnum target, String name) {
		if (codeToLegoCommandCodeMapping == null) {
			codeToLegoCommandCodeMapping = initMapping();
		}

		return codeToLegoCommandCodeMapping.entrySet().stream().map(Map.Entry::getValue)
				.filter(v -> v.getTarget().equals(target)).filter(v -> v.getName().equals(name))
				.reduce(null, (e1, e2) -> e2);
	}

	// Private Methods
	private static Map<Integer, RequestCommandEnum> initMapping() {
		return Arrays.stream(values()).collect(Collectors.toMap(RequestCommandEnum::getCode, e -> e));
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

	public CommandTargetEnum getTarget() {
		return target;
	}

	@Override
	public String toString() {
		return "RequestCommandEnum{" + "code=" + code + ", target=" + target + ", name='" + name + '\'' + '}';
	}
}
