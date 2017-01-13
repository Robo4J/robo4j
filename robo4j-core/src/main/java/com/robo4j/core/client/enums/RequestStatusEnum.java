/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This RequestStatusEnum.java  is part of robo4j.
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

/**
 * Request Status is the resul done by the Request
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 13.11.2016
 */
public enum RequestStatusEnum {

	// formatter:off
	NONE(-1, "none"), EXIT(0, "exit"), ACTIVE(1, "active"),;
	// formatter:on

	private int code;
	private String desc;

	RequestStatusEnum(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

	@Override
	public String toString() {
		return "RequestStatusEnum{" + "code=" + code + ", desc='" + desc + '\'' + '}';
	}
}
