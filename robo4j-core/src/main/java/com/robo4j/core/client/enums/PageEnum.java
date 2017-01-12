/*
 * Copyright (C)  2016. Miroslav Wengner and Marcus Hirt
 * This PageEnum.java  is part of robo4j.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Solution how to display web pages
 *
 * @author Miro Wengner (@miragemiko)
 * @since 10.08.2016
 */
public enum PageEnum {

	// @formatter:on
	WELCOME(0, "welcome", "welcome.html", null), STATUS(1, "status", "status.html",
			Collections.singletonList("cache")), SUCCESS(2, "success", "success.html",
					Collections.singletonList("command")), ERROR(3, "error", "error.html", null), SETUP(4, "setup",
							"setup.html", Collections.singletonList("setup")), EXIT(5, "exit", "exit.html", null),;
	// @formatter:on

	private volatile static Map<String, PageEnum> nameToPageMapping;
	private int code;
	private String name;
	private String page;
	private List<String> variables;

	PageEnum(int code, String name, String page, List<String> variables) {
		this.code = code;
		this.name = name;
		this.page = page;
		this.variables = variables;
	}

	public static PageEnum getPageEnumByName(final String name) {
		if (nameToPageMapping == null) {
			nameToPageMapping = mappingInit();
		}
		return nameToPageMapping.get(name);
	}

	// Private Methods
	private static Map<String, PageEnum> mappingInit() {
		return Arrays.stream(values()).collect(Collectors.toMap(PageEnum::getName, e -> e));
	}

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getPage() {
		return page;
	}

	public List<String> getVariables() {
		return variables;
	}
}
