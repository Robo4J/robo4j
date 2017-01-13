/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This HttpUtils.java  is part of robo4j.
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

package com.robo4j.core.client.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.robo4j.commons.command.CommandTargetEnum;
import com.robo4j.commons.logging.SimpleLoggingUtil;
import com.robo4j.core.client.enums.RequestCommandEnum;
import com.robo4j.core.dto.ClientCommandRequestDTO;
import com.robo4j.core.dto.ClientRequestDTO;
import com.robo4j.core.dto.ClientUnitRequestDTO;
import com.robo4j.core.dto.HttpRequestElementDTO;
import com.robo4j.core.util.ConstantUtil;

/**
 *
 * Basic Http constants and utils methods
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 23.05.2016
 */
public final class HttpUtils {

	public static final String HTTP_HEADER_OK = "HTTP/1.0 200 OK";
	public static final String HTTP_HEADER_NOT = "HTTP/1.0 501 Not Implemented";
	public static final String HTTP_HEADER_NOT_ALLOWED = "HTTP/1.0 405 Method Not Allowed";
	/* command represent command line order */
	private static final String HTTP_COMMAND = "command";
	/* commands represent list of commands */
	private static final String HTTP_COMMANDS = "commands";
	private static final String HTTP_UNITS = "units";
	private static final String HTTP_GENERAL = "general";

	private static final String HTTP_REQUEST_VALUE = "value";
	private static final String NEXT_LINE = "\r\n";
	private static final int POST_COMMAND_SEP = 2;

	public static String setHeader(String responseCode, int length) throws IOException {
		return new StringBuilder(ConstantUtil.EMPTY_STRING).append(responseCode).append(NEXT_LINE).append("Date: ")
				.append(LocalDateTime.now()).append(NEXT_LINE).append("Server: robo4j-client").append(NEXT_LINE)
				.append("Content-length: ").append(length).append(NEXT_LINE)
				.append("Content-type: text/html; charset=utf-8").append(NEXT_LINE).append(NEXT_LINE).toString();
	}

	/**
	 * Parsing received buffer to the list of ClientRequestCommands
	 */
	public static ClientRequestDTO transformToCommands(final String buffer) throws ParseException {
		final JSONParser parser = new JSONParser();
		final JSONObject request = (JSONObject) parser.parse(String.valueOf(buffer));
		final List<ClientCommandRequestDTO> commands = new LinkedList<>();
		final List<ClientUnitRequestDTO> units = new LinkedList<>();

		// TODO :: need to do review
		SimpleLoggingUtil.debug(HttpUtils.class, "Request:: " + request);
		getValidCommandElements(request).forEach(e -> {
			switch (e.getName()) {
			case HTTP_GENERAL:
				SimpleLoggingUtil.debug(HttpUtils.class, "http_general: " + request.get(HTTP_GENERAL));
				break;
			case HTTP_COMMAND:
				commands.addAll(parseURIQuery(request.get(HTTP_COMMAND).toString(),
						ConstantUtil.getHttpSeparator(POST_COMMAND_SEP)));
				break;
			case HTTP_COMMANDS:
				commands.addAll(parseJSONToCommandsArray((JSONArray) request.get(HTTP_COMMANDS)));
				break;
			case HTTP_UNITS:
				units.addAll(parseJSONToUnitsArrays((JSONArray) request.get(HTTP_UNITS)));
				SimpleLoggingUtil.debug(HttpUtils.class, "Update Units: " + units);
				break;
			default:
				break;
			}
		});
		final ClientRequestDTO result = new ClientRequestDTO(commands, units);
		SimpleLoggingUtil.debug(HttpUtils.class, "transformToCommand: " + result);
		return result;
	}

	public static List<ClientCommandRequestDTO> parseURIQuery(final String uriQuery, final String delimiter) {
		return Arrays.stream(uriQuery.split(delimiter)).filter(e -> !e.isEmpty()).map(ClientCommandRequestDTO::new)
				.collect(Collectors.toCollection(LinkedList::new));
	}

	// Private Methods
	/* commands array is preferred way to address commands */
	private static List<HttpRequestElementDTO> getValidCommandElements(final JSONObject request) {
		final List<HttpRequestElementDTO> result = new ArrayList<>();
		if (request.containsKey(HTTP_COMMAND) && !request.get(HTTP_COMMAND).toString().isEmpty()) {
			result.add(new HttpRequestElementDTO(HTTP_COMMAND, request.get(HTTP_COMMAND).toString()));
		}
		if (request.containsKey(HTTP_COMMANDS) && !((JSONArray) request.get(HTTP_COMMANDS)).isEmpty()) {
			result.add(new HttpRequestElementDTO(HTTP_COMMANDS, request.get(HTTP_COMMANDS)));
		}
		if (request.containsKey(HTTP_UNITS) && !((JSONArray) request.get(HTTP_UNITS)).isEmpty()) {
			result.add(new HttpRequestElementDTO(HTTP_UNITS, request.get(HTTP_UNITS)));
		}

		return result;
	}

	// TODO: can be standardised
	@SuppressWarnings(value = "unchecked")
	private static List<ClientCommandRequestDTO> parseJSONToCommandsArray(final JSONArray jsonArray) {
		return (List<ClientCommandRequestDTO>) jsonArray.stream().map(e -> {
			JSONObject obj = (JSONObject) e;
			final String target = obj.get("target").toString();
			final String name = obj.get("name").toString();
			final String value = obj.containsKey(HTTP_REQUEST_VALUE) ? obj.get(HTTP_REQUEST_VALUE).toString()
					: ConstantUtil.EMPTY_STRING;
			RequestCommandEnum command = RequestCommandEnum.getRequestCommand(CommandTargetEnum.getByName(target),
					name);
			if (obj.containsKey("speed")) {
				final String speed = obj.get("speed").toString();
				return new ClientCommandRequestDTO(command, value, speed);
			} else {
				return new ClientCommandRequestDTO(command, value);
			}

		}).collect(Collectors.toCollection(LinkedList::new));
	}

	@SuppressWarnings(value = "unchecked")
	private static List<ClientUnitRequestDTO> parseJSONToUnitsArrays(final JSONArray jsonArray) {
		return (List<ClientUnitRequestDTO>) jsonArray.stream().map(e -> {
			JSONObject obj = (JSONObject) e;
			final String name = obj.get("name").toString();
			final Boolean active = Objects.nonNull(obj.get("active")) ? Boolean.valueOf(obj.get("active").toString())
					: false;
			return new ClientUnitRequestDTO(name, active);
		}).collect(Collectors.toList());
	}

}
