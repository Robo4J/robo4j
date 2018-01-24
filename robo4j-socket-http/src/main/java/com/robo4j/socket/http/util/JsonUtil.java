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

import com.robo4j.socket.http.HttpException;
import com.robo4j.socket.http.dto.ClientPathDTO;
import com.robo4j.socket.http.dto.ResponseUnitDTO;
import com.robo4j.socket.http.json.JsonDocument;
import com.robo4j.socket.http.json.JsonReader;
import com.robo4j.util.Utf8Constant;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.robo4j.util.Utf8Constant.DEFAULT_ENCODING;
import static com.robo4j.util.Utf8Constant.UTF8_COLON;
import static com.robo4j.util.Utf8Constant.UTF8_COMMA;
import static com.robo4j.util.Utf8Constant.UTF8_CURLY_BRACKET_LEFT;
import static com.robo4j.util.Utf8Constant.UTF8_CURLY_BRACKET_RIGHT;
import static com.robo4j.util.Utf8Constant.UTF8_SQUARE_BRACKET_LEFT;
import static com.robo4j.util.Utf8Constant.UTF8_SQUARE_BRACKET_RIGHT;

/**
 *
 * Simple Json util
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class JsonUtil {

	public static final Set<Class<?>> WITHOUT_QUOTATION_TYPES = Stream.of(boolean.class, int.class, short.class,
			byte.class, long.class, double.class, float.class, char.class, Boolean.class, Integer.class, Short.class,
			Byte.class, Long.class, Double.class, Float.class, Character.class).collect(Collectors.toSet());
	public static final Set<Class<?>> QUOTATION_TYPES = Stream.of(String.class).collect(Collectors.toSet());
	private static final String DELIMITER_JSON_OBJECTS = "(?<=\\})(?=\\,\\{)";
	public static final String PATTERN_OBJ_FROM_ARRAY = "^\\[(.*)\\]$";
	public static final String FIELD_ID = "id";

	public static String bytesToBase64String(byte[] array) {
		try {
			return new String(Base64.getEncoder().encode(array), DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new HttpException("image capture", e);
		}
	}


	/**
	 * Converting json {"imageProcessor":["POST","callBack"]} to ClientPathDTO
	 *
	 * @param json
	 *            - string
	 * @return List of elements
	 */
	public static ClientPathDTO getPathMethodByJson(String json) {
		JsonReader jsonReader = new JsonReader(json);
		JsonDocument document = jsonReader.read();
		return ReflectUtils.createInstanceByClazzAndDescriptorAndJsonDocument(ClientPathDTO.class, document);
	}

	public static String getJsonByPathMethodList(List<ClientPathDTO> pathMethodList) {
		final JsonElementStringBuilder builder = JsonElementStringBuilder.Builder().add(Utf8Constant.UTF8_SQUARE_BRACKET_LEFT);
		if(!pathMethodList.isEmpty()){
			//@formatter:off
			builder.add(pathMethodList.stream()
					.map(ReflectUtils::createJson)
					.collect(Collectors.joining(Utf8Constant.UTF8_COMMA)));
			//@formatter:on
		}
		return builder.add(UTF8_SQUARE_BRACKET_RIGHT)
				.build();
	}

	/**
	 * Create desired Json String: {"imageProcessor":["POST","callBack"]}
	 *
	 * @param pathMethod
	 *            valid method
	 * @return jsonString
	 */
	public static String getJsonByPathMethod(ClientPathDTO pathMethod) {
		JsonElementStringBuilder builder = JsonElementStringBuilder.Builder().add(UTF8_CURLY_BRACKET_LEFT)
				.addQuotationWithDelimiter(UTF8_COLON, pathMethod.getRoboUnit()).add(UTF8_SQUARE_BRACKET_LEFT);
		if (pathMethod.getCallbacks() == null) {
			builder.addQuotation(pathMethod.getMethod().getName());
		} else {
			builder.addQuotationWithDelimiter(UTF8_COMMA, pathMethod.getMethod().getName())
					.add(UTF8_SQUARE_BRACKET_LEFT);
			pathMethod.getCallbacks().forEach(builder::addQuotation);
			builder.add(UTF8_SQUARE_BRACKET_RIGHT);
		}

		//@formatter:off
		return builder.add(UTF8_SQUARE_BRACKET_RIGHT)
				.add(UTF8_CURLY_BRACKET_RIGHT)
				.build();
		//@formatter:on
	}

	/**
	 * convert targetUnit json,
	 * [{"imageController":["POST","callbackPOSTController"]},...] to the PathMethod
	 * List List doesn't contain duplicates
	 *
	 *
	 * [{"imageController":["POST","callbackPOSTController"]},{"imageController":["POST","callbackPOSTController"]}]
	 *
	 * @param json
	 *            targetUnit json
	 * @return extracted List
	 */
	public static List<ClientPathDTO> convertJsonToPathMethodList(String json) {
		JsonReader jsonReader = new JsonReader(json);
		JsonDocument document = jsonReader.read();
		//@formatter:off
		return document.getArray().stream()
				.map(e -> ReflectUtils.createInstanceByClazzAndDescriptorAndJsonDocument(ClientPathDTO.class, (JsonDocument) e))
				.collect(Collectors.toCollection(LinkedList::new));
		//@formatter:on
	}


	public static String getArrayByListResponseUnitDTO(List<ResponseUnitDTO> units) {
		return JsonElementStringBuilder.Builder().add(Utf8Constant.UTF8_SQUARE_BRACKET_LEFT)
				.add(units.stream()
						.map(unit -> JsonElementStringBuilder.Builder().add(Utf8Constant.UTF8_CURLY_BRACKET_LEFT)
								.addQuotationWithDelimiter(Utf8Constant.UTF8_COLON, FIELD_ID)
								.addQuotationWithDelimiter(Utf8Constant.UTF8_COMMA, unit.getId())
								.addQuotationWithDelimiter(Utf8Constant.UTF8_COLON,
										unit.getState().getClass().getCanonicalName())
								.addQuotation(unit.getState().getLocalizedName().toUpperCase())
								.add(Utf8Constant.UTF8_CURLY_BRACKET_RIGHT).build())
						.collect(Collectors.joining(Utf8Constant.UTF8_COMMA)))
				.add(Utf8Constant.UTF8_SQUARE_BRACKET_RIGHT).build();
	}
}
