/*
 * Copyright (c) 2014-2019, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.socket.http.util;

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.ClassGetSetDTO;
import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.util.Utf8Constant;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Util class used to build json path configuration by separated paths
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class HttpPathConfigJsonBuilder {

	private final Map<String, ClassGetSetDTO> descriptorMap;
	private final List<HttpPathMethodDTO> paths = new LinkedList<>();

	private HttpPathConfigJsonBuilder() {
		descriptorMap = ReflectUtils.getFieldsTypeMap(HttpPathMethodDTO.class);
	}

	public static HttpPathConfigJsonBuilder Builder() {
		return new HttpPathConfigJsonBuilder();
	}

	public HttpPathConfigJsonBuilder addPath(String roboUnit, HttpMethod method) {
		HttpPathMethodDTO document = new HttpPathMethodDTO(roboUnit, method);
		paths.add(document);
		return this;
	}

	public HttpPathConfigJsonBuilder addPath(String roboUnit, HttpMethod method, List<String> filters) {
		HttpPathMethodDTO document = new HttpPathMethodDTO(roboUnit, method, filters);
		paths.add(document);
		return this;
	}

	public String build() {
		return new StringBuilder().append(Utf8Constant.UTF8_SQUARE_BRACKET_LEFT)
				.append(paths.stream().map(e -> ReflectUtils.createJson(descriptorMap, e))
						.collect(Collectors.joining(Utf8Constant.UTF8_COMMA)))
				.append(Utf8Constant.UTF8_SQUARE_BRACKET_RIGHT).toString();
	}
}
