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
import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.socket.http.enums.SystemPath;
import com.robo4j.socket.http.json.JsonDocument;
import com.robo4j.socket.http.json.JsonReader;
import com.robo4j.socket.http.units.ClientContext;
import com.robo4j.socket.http.units.ClientPathConfig;
import com.robo4j.socket.http.units.PathHttpMethod;
import com.robo4j.socket.http.units.ServerContext;
import com.robo4j.socket.http.units.ServerPathConfig;
import com.robo4j.util.StringConstants;
import com.robo4j.util.Utf8Constant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utils for the path operation
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class HttpPathUtils {

	public static final String ATTRIBUTES_PATH_VALUE = "attributes";
	public static final String DELIMITER_ATTRIBUTE_KEY_VALUE = "=";
	public static final String DELIMITER_ATTRIBUTES = ",";
	public static final String DELIMITER_PATH_ATTRIBUTES = "?";
	public static final String REGEX_ATTRIBUTE = "\\?";
	public static final String REGEX_ATTRIBUTE_CONCAT = "&";

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
	public static HttpPathMethodDTO readServerPathDTO(String configurationJson) {
		Class<HttpPathMethodDTO> clazz = HttpPathMethodDTO.class;
		JsonReader jsonReader = new JsonReader(configurationJson);
		JsonDocument document = jsonReader.read();
		return ReflectUtils.createInstanceByClazzAndDescriptorAndJsonDocument(clazz, document);
	}

	/**
	 * transform path dto to proper http server path
	 *
	 * @param dto
	 *            path DTO
	 * @param reference
	 *            roboReference related to the path
	 * @return server unit path config
	 */
	public static ServerPathConfig toHttpPathConfig(HttpPathMethodDTO dto, RoboReference<Object> reference) {
		final String unitPath = toPath(SystemPath.UNITS.getPath(), dto.getRoboUnit());
		return new ServerPathConfig(unitPath, reference, dto.getMethod(), dto.getCallbacks());
	}

	public static ClientPathConfig toClientPathConfig(HttpPathMethodDTO dto) {
		final String unitPath = dto.getRoboUnit().equals(StringConstants.EMPTY) ? Utf8Constant.UTF8_SOLIDUS
				: toPath(SystemPath.UNITS.getPath(), dto.getRoboUnit());
		final List<String> callbacks = dto.getCallbacks() == null ? new ArrayList<>() : dto.getCallbacks();
		return new ClientPathConfig(unitPath, dto.getMethod(), callbacks);
	}

	public static void updateHttpServerContextPaths(final RoboContext context, final ServerContext serverContext,
			final Collection<HttpPathMethodDTO> paths) {
		final Map<PathHttpMethod, ServerPathConfig> resultPaths = paths.stream().map(e -> {
			RoboReference<Object> reference = context.getReference(e.getRoboUnit());
			return HttpPathUtils.toHttpPathConfig(e, reference);
		}).collect(Collectors.toMap(e -> new PathHttpMethod(e.getPath(), e.getMethod()), e -> e));

		resultPaths.put(new PathHttpMethod(Utf8Constant.UTF8_SOLIDUS, HttpMethod.GET),
				new ServerPathConfig(Utf8Constant.UTF8_SOLIDUS, null, HttpMethod.GET));
		serverContext.addPaths(resultPaths);
	}

	public static void updateHttpClientContextPaths(final ClientContext clientContext,
			final Collection<HttpPathMethodDTO> paths) {
		final Map<PathHttpMethod, ClientPathConfig> resultPaths = paths.stream().map(HttpPathUtils::toClientPathConfig)
				.collect(Collectors.toMap(e -> new PathHttpMethod(e.getPath(), e.getMethod()), e -> e));
		clientContext.addPaths(resultPaths);
	}

	public static Map<String, Set<String>> extractAttributesByPath(String path) {
		return Stream.of(path.split(REGEX_ATTRIBUTE)[1].split(REGEX_ATTRIBUTE_CONCAT))
				.map(e -> e.split(DELIMITER_ATTRIBUTE_KEY_VALUE))
				.collect(Collectors.toMap(e -> e[0],
						e -> e.length > 1 && e[1] != null
								? Stream.of(e[1].split(DELIMITER_ATTRIBUTES)).collect(Collectors.toSet())
								: Collections.emptySet()));
	}

}
