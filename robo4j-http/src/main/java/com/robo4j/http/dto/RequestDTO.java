/*
 * Copyright (C) 2016. Miroslav Wengner, Marcus Hirt
 * This GetRequestDTO.java is part of robo4j.
 *
 *     robo4j is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     robo4j is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.http.dto;

import java.util.Map;

/**
 * @author Miroslav Wengner (@miragemiko)
 * @since 17.10.2016
 */
public class RequestDTO {

	private String path;
	private Map<String, String> values;

	public RequestDTO(String path, Map<String, String> values) {
		this.path = path;
		this.values = values;
	}

	public String getPath() {
		return path;
	}

	public Map<String, String> getParameters() {
		return values;
	}

	@Override
	public String toString() {
		return "RequestDTO{" + "path='" + path + '\'' + ", values=" + values + '}';
	}
}
