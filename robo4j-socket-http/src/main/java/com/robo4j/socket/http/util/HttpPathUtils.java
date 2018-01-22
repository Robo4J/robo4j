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

import com.robo4j.socket.http.dto.ServerPathDTO;
import com.robo4j.socket.http.json.JsonDocument;
import com.robo4j.socket.http.json.JsonReader;
import com.robo4j.socket.http.units.ServerPathConfig;
import com.robo4j.socket.http.units.ServerPathMethod;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utils for the path operation
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class HttpPathUtils {
	private static final int SEPARATOR_PATH = 12;

	/**
	 *
	 * @param source
	 *            complete URI as string
	 * @return list of paths
	 */
	public static List<String> uriStringToPathList(String source) {
		return Stream.of(source.split(HttpMessageUtils.getHttpSeparator(SEPARATOR_PATH))).filter(e -> !e.isEmpty())
				.collect(Collectors.toList());
	}

	public static String pathsToUri(List<String> paths) {
		return paths.stream().collect(Collectors.joining(HttpMessageUtils.getHttpSeparator(SEPARATOR_PATH)));
	}

	/**
	 * parse json string to mutable path properties
	 * @param configurationJson configuration json
	 * @return return server path dto with method and possible properties
	 */
	public static ServerPathDTO readServerPathDTO(String configurationJson){
		Class<ServerPathDTO> clazz = ServerPathDTO.class;
		JsonReader jsonReader = new JsonReader(configurationJson);
		JsonDocument document = jsonReader.read();
		return ReflectUtils.createInstanceByClazzAndDescriptorAndJsonDocument(clazz, document);
	}

	@SuppressWarnings("unchecked")
	public static ServerPathConfig readHttpServerPathConfig(String configurationJson){
		if(configurationJson == null || configurationJson.isEmpty()){
			return new ServerPathConfig(Collections.emptyList());
		}
		final Class<ServerPathDTO> clazz = ServerPathDTO.class;
		final JsonReader jsonReader = new JsonReader(configurationJson);
		final JsonDocument document = jsonReader.read();

		final List<ServerPathMethod> serverPathMethods = document.getArray().stream()
				.map(JsonDocument.class::cast)
				.map(e -> ReflectUtils.createInstanceByClazzAndDescriptorAndJsonDocument(clazz, e))
				.map(HttpPathUtils::toServerPathMethod)
				.collect(Collectors.toCollection(LinkedList::new));

		return new ServerPathConfig(serverPathMethods);
	}

	public static ServerPathMethod toServerPathMethod(ServerPathDTO dto){
		return new ServerPathMethod(dto.getRoboUnit(), dto.getMethod(), dto.getFilters());
	}

}
