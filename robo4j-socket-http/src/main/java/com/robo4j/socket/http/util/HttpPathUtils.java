/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.ServerUnitPathDTO;
import com.robo4j.socket.http.enums.SystemPath;
import com.robo4j.socket.http.json.JsonDocument;
import com.robo4j.socket.http.json.JsonReader;
import com.robo4j.socket.http.units.ServerContext;
import com.robo4j.socket.http.units.ServerPathConfig;
import com.robo4j.util.Utf8Constant;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utils for the path operation
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class HttpPathUtils {
	private static final int SEPARATOR_PATH = 12;

	public static String toPath(String first, String... rest) {
		final StringBuilder sb = new StringBuilder();
		sb.append(Utf8Constant.UTF8_SOLIDUS).append(first);
		if (rest.length > 0) {
			for (String s : rest) {
				sb.append(Utf8Constant.UTF8_SOLIDUS).append(s);
			}
		}
		return sb.toString();
	}

	public static String pathsToUri(List<String> paths) {
		return paths.stream().collect(Collectors.joining(HttpMessageUtils.getHttpSeparator(SEPARATOR_PATH)));
	}

	/**
	 * parse json string to mutable path properties
	 * 
	 * @param configurationJson
	 *            configuration json
	 * @return return server path dto with method and possible properties
	 */
	public static ServerUnitPathDTO readServerPathDTO(String configurationJson) {
		Class<ServerUnitPathDTO> clazz = ServerUnitPathDTO.class;
		JsonReader jsonReader = new JsonReader(configurationJson);
		JsonDocument document = jsonReader.read();
		return ReflectUtils.createInstanceByClazzAndDescriptorAndJsonDocument(clazz, document);
	}

	@SuppressWarnings("unchecked")
	public static List<ServerUnitPathDTO> readPathConfig(String configurationJson) {
		if (configurationJson == null || configurationJson.isEmpty()) {
			return Collections.emptyList();
		}
		final Class<ServerUnitPathDTO> clazz = ServerUnitPathDTO.class;
		final JsonReader jsonReader = new JsonReader(configurationJson);
		final JsonDocument document = jsonReader.read();

		//@formatter:off
		return document.getArray().stream().map(JsonDocument.class::cast)
				.map(e -> ReflectUtils.createInstanceByClazzAndDescriptorAndJsonDocument(clazz, e))
				.collect(Collectors.toCollection(LinkedList::new));
		//@formatter:on
	}

	public static ServerPathConfig toServerUnitPathMethod(ServerUnitPathDTO dto, RoboReference<Object> reference) {
		final String unitPath = new StringBuilder().append(Utf8Constant.UTF8_SOLIDUS).append(SystemPath.UNITS.getPath())
				.append(Utf8Constant.UTF8_SOLIDUS).append(dto.getRoboUnit()).toString();
		return new ServerPathConfig(unitPath, reference, dto.getMethod(), dto.getFilters());
	}

	/**
	 * Server context
	 * 
	 * @param context
	 *            initiated roboContext
	 * @param paths
	 *            available configured paths
	 * @return server context
	 */
	public static ServerContext initServerContext(final RoboContext context, final List<ServerUnitPathDTO> paths) {
		final Map<String, ServerPathConfig> result = paths.stream().map(e -> {
			RoboReference<Object> reference = context.getReference(e.getRoboUnit());
			return HttpPathUtils.toServerUnitPathMethod(e, reference);
		}).collect(Collectors.toMap(ServerPathConfig::getPath, e -> e));
		result.put(Utf8Constant.UTF8_SOLIDUS, new ServerPathConfig(Utf8Constant.UTF8_SOLIDUS, null, HttpMethod.GET));
		return new ServerContext(result);
	}

}
