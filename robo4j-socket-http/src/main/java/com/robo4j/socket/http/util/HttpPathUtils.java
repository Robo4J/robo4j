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
import com.robo4j.socket.http.dto.ClientPathDTO;
import com.robo4j.socket.http.dto.ServerUnitPathDTO;
import com.robo4j.socket.http.enums.SystemPath;
import com.robo4j.socket.http.json.JsonDocument;
import com.robo4j.socket.http.json.JsonReader;
import com.robo4j.socket.http.units.ClientPathConfig;
import com.robo4j.socket.http.units.ServerContentBuilder;
import com.robo4j.socket.http.units.ServerContext;
import com.robo4j.socket.http.units.ServerPathConfig;
import com.robo4j.util.Utf8Constant;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utils for the path operation
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class HttpPathUtils {

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
	public static <T> List<T> readPathConfig(Class<T> clazz, String configurationJson) {
		if (configurationJson == null || configurationJson.isEmpty()) {
			return Collections.emptyList();
		}
		final JsonDocument document = parseJsonByClass(configurationJson);

		//@formatter:off
		return document.getArray().stream().map(JsonDocument.class::cast)
				.map(e -> ReflectUtils.createInstanceByClazzAndDescriptorAndJsonDocument(clazz, e))
				.collect(Collectors.toCollection(LinkedList::new));
		//@formatter:on
	}

	public static ServerPathConfig toServerPathConfig(ServerUnitPathDTO dto, RoboReference<Object> reference) {
		final String unitPath = toPath(SystemPath.UNITS.getPath(), dto.getRoboUnit());
		return new ServerPathConfig(unitPath, reference, dto.getMethod(), dto.getFilters());
	}

	public static ClientPathConfig toClientPathConfig(ClientPathDTO dto, List<RoboReference<Object>> references){
		final String unitPath = toPath(SystemPath.UNITS.getPath(), dto.getRoboUnit());
		return new ClientPathConfig(unitPath, dto.getMethod(), references);
	}

	public static JsonDocument parseJsonByClass(String json) {
		final JsonReader jsonReader = new JsonReader(json);
		return jsonReader.read();
	}

	/**
	 * Server context, context does contain default path /
	 * 
	 * @param context
	 *            initiated roboContext
	 * @param paths
	 *            available configured paths
	 * @return server context
	 */
	public static ServerContext initServerContext(final RoboContext context, final List<ServerUnitPathDTO> paths) {
		return ServerContentBuilder.Builder().addPaths(paths).build(context);
	}

}
