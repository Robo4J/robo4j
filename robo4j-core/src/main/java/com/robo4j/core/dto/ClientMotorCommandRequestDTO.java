/*
 * Copyright (C) 2016, 2017. Miroslav Wengner, Marcus Hirt
 * This ClientCommandRequestDTO.java  is part of robo4j.
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

package com.robo4j.core.dto;

import com.robo4j.core.command.PlatformUnitCommandEnum;
import com.robo4j.core.util.ConstantUtil;

/**
 *
 * Client Request Holder for incoming requests Single instance
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 11.06.2016
 */

//TODO: FIXME refactor -> separate
public class ClientMotorCommandRequestDTO implements ClientCommandDTO<PlatformUnitCommandEnum> {

	private Long stamp;
	private PlatformUnitCommandEnum command;
	private String value;
	private String speed;

	public ClientMotorCommandRequestDTO(String value) {
		this.stamp = System.currentTimeMillis();
		final String[] values = value.split(ConstantUtil.getHttpSeparator(3));
		this.command = PlatformUnitCommandEnum.getCommand(values[ConstantUtil.DEFAULT_VALUE_0]);
		this.value = values[1];
		this.speed = "300";
	}

	public ClientMotorCommandRequestDTO(PlatformUnitCommandEnum command) {
		this.stamp = System.currentTimeMillis();
		this.command = command;
		this.value = "";
		this.speed = "200";
	}

	public ClientMotorCommandRequestDTO(PlatformUnitCommandEnum command, String value) {
		this.stamp = System.currentTimeMillis();
		this.command = command;
		this.value = value;
		this.speed = "200";
	}

	public ClientMotorCommandRequestDTO(PlatformUnitCommandEnum command, String value, String speed) {
		this.stamp = System.currentTimeMillis();
		this.command = command;
		this.value = value;
		this.speed = speed;
	}

	public String getSpeed() {
		return speed;
	}

	public Long getStamp() {
		return stamp;
	}

	@Override
	public PlatformUnitCommandEnum getCommand() {
		return command;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "ClientCommandRequestDTO{" + "stamp=" + stamp + ", command=" + command + ", value='" + value + '\''
				+ ", speed='" + speed + '\'' + '}';
	}
}
