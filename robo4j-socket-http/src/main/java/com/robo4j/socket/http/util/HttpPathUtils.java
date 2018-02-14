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
import com.robo4j.socket.http.dto.ClientPathDTO;
import com.robo4j.socket.http.dto.ServerUnitPathDTO;
import com.robo4j.socket.http.enums.SystemPath;
import com.robo4j.socket.http.json.JsonDocument;
import com.robo4j.socket.http.json.JsonReader;
import com.robo4j.socket.http.units.ClientContext;
import com.robo4j.socket.http.units.ClientPathConfig;
import com.robo4j.socket.http.units.ServerContext;
import com.robo4j.socket.http.units.ServerPathConfig;
import com.robo4j.util.StringConstants;
import com.robo4j.util.Utf8Constant;

import java.util.Collection;
import java.util.Map;
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

	/**
	 * transform path dto to proper http server path
	 *
	 * @param dto path DTO
	 * @param reference roboReference related to the path
	 * @return server unit path config
	 */
	public static ServerPathConfig toServerPathConfig(ServerUnitPathDTO dto, RoboReference<Object> reference) {
		final String unitPath = toPath(SystemPath.UNITS.getPath(), dto.getRoboUnit());
		return new ServerPathConfig(unitPath, reference, dto.getMethod(), dto.getFilters());
	}

	public static ClientPathConfig toClientPathConfig(ClientPathDTO dto) {
		final String unitPath = dto.getRoboUnit().equals(StringConstants.EMPTY) ? Utf8Constant.UTF8_SOLIDUS
				: toPath(SystemPath.UNITS.getPath(), dto.getRoboUnit());
		return new ClientPathConfig(unitPath, dto.getMethod(), dto.getCallbacks());
	}

	public static void updateHttpServerContextPaths(final RoboContext context, final ServerContext serverContext, final Collection<ServerUnitPathDTO> paths){
		final Map<String, ServerPathConfig> resultPaths = paths.stream().map(e -> {
			RoboReference<Object> reference = context.getReference(e.getRoboUnit());
			return HttpPathUtils.toServerPathConfig(e, reference);
		}).collect(Collectors.toMap(ServerPathConfig::getPath, e -> e));
		resultPaths.put(Utf8Constant.UTF8_SOLIDUS,
				new ServerPathConfig(Utf8Constant.UTF8_SOLIDUS, null, HttpMethod.GET));
		serverContext.addPaths(resultPaths);
	}



	public static void updateHttpClientContextPaths(final ClientContext clientContext, final Collection<ClientPathDTO> paths){
		final Map<String, ClientPathConfig> resultPaths = paths.stream()
				.map(HttpPathUtils::toClientPathConfig)
				.collect(Collectors.toMap(ClientPathConfig::getPath, e -> e));
		clientContext.addPaths(resultPaths);
	}

}
