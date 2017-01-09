/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This AgentStatusEnum.java  is part of robo4j.
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

package com.robo4j.commons.agent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Miro Kopecky (@miragemiko)
 * @since 29.05.2016
 */
public enum AgentStatusEnum {

	// @formatter:off
	READY(0, "ready"), ACTIVE(1, "active"), OFFLINE(2, "offline"), REQUEST_POST(3, "request_post"), REQUEST_GET(4,
			"request_get");
	// @formatter:on

	private volatile static Map<String, AgentStatusEnum> defToAgentStatusMapping;
	private int code;
	private String def;

	AgentStatusEnum(int code, String def) {
		this.code = code;
		this.def = def;
	}

	private static void initMapping() {
		defToAgentStatusMapping = new HashMap<>();
		for (AgentStatusEnum ct : values()) {
			defToAgentStatusMapping.put(ct.getDef(), ct);
		}
	}

	public static AgentStatusEnum getByState(String def) {
		if (defToAgentStatusMapping == null)
			initMapping();
		return defToAgentStatusMapping.get(def);
	}

	public Integer getCode() {
		return code;
	}

	public String getDesc() {
		return def;
	}

	public String getDef() {
		return def;
	}

	@Override
	public String toString() {
		return "AgentStatusEnum{" + "code=" + code + ", def='" + def + '\'' + '}';
	}
}
