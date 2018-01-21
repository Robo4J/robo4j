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

import com.robo4j.LifecycleState;
import com.robo4j.socket.http.HttpException;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.PathMethodDTO;
import com.robo4j.socket.http.dto.ResponseUnitDTO;
import com.robo4j.util.StringConstants;
import com.robo4j.util.Utf8Constant;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.robo4j.util.Utf8Constant.DEFAULT_ENCODING;
import static com.robo4j.util.Utf8Constant.UTF8_COLON;
import static com.robo4j.util.Utf8Constant.UTF8_COMMA;
import static com.robo4j.util.Utf8Constant.UTF8_CURLY_BRACKET_LEFT;
import static com.robo4j.util.Utf8Constant.UTF8_CURLY_BRACKET_RIGHT;
import static com.robo4j.util.Utf8Constant.UTF8_QUOTATION_MARK;
import static com.robo4j.util.Utf8Constant.UTF8_SOLIDUS;
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

	@SuppressWarnings(value = "unchecked")
	public static String getJsonByMap(Map<String, Object> map) {
		final StringBuilder sb = new StringBuilder(UTF8_CURLY_BRACKET_LEFT);
		sb.append(map.entrySet().stream().map(e -> {
			StringBuilder sb2 = new StringBuilder(UTF8_QUOTATION_MARK).append(e.getKey()).append(UTF8_QUOTATION_MARK)
					.append(UTF8_COLON);
			Class<?> clazz = e.getValue().getClass();
			if (checkPrimitiveOrWrapper(clazz)) {
				sb2.append(e.getValue());
			} else if (checkString(clazz)) {
				sb2.append(UTF8_QUOTATION_MARK).append(e.getValue()).append(UTF8_QUOTATION_MARK);
			} else if (e.getValue() instanceof Enum<?>) {
				Enum<?> en = (Enum<?>) e.getValue();
				sb2.append(UTF8_QUOTATION_MARK).append(en.name()).append(UTF8_QUOTATION_MARK);
			} else if (e.getValue() instanceof Map) {
				sb2.append(getJsonByMap((Map<String, Object>) e.getValue()));
			}
			return sb2.toString();
		}).collect(Collectors.joining(UTF8_COMMA))).append(UTF8_CURLY_BRACKET_RIGHT);
		return sb.toString();
	}

	public static Map<String, Object> getMapByJson(String json) {
		final Map<String, Object> result = new LinkedHashMap<>();
		if (json == null) {
			return result;
		}

		final String extractedObject = json.replaceAll("^\\{\\s*\"|\"?\\s*\\}$", StringConstants.EMPTY);
		final String[] parts = extractedObject.split("\"?(\"?\\s*:\\s*\"?|\\s*,\\s*)\"?");

		for (int i = 0; i < parts.length - 1; i += 2) {
			String keyPart = parts[i].trim();
			String valuePart = parts[i + 1].trim();
			if (valuePart.startsWith(UTF8_CURLY_BRACKET_LEFT)) {
				result.put(keyPart, getMapByJson(valuePart));
			} else if (valuePart.startsWith(UTF8_SQUARE_BRACKET_LEFT)) {
				Object[] preArray = valuePart.replaceAll("^\\[\\s*|\\s*\\]$", StringConstants.EMPTY).split(UTF8_COMMA);
				result.put(keyPart,
						Stream.of(preArray).map(JsonUtil::parseJsonStringToObject).collect(Collectors.toList()));
			} else {
				result.put(keyPart, valuePart);
			}
		}
		return result;

	}

	public static String parseJsonStringToObject(Object obj) {
		return obj.toString().replaceAll(UTF8_QUOTATION_MARK, StringConstants.EMPTY);
	}

	public static JsonElementStringBuilder getInitJsonBuilder() {
		return JsonElementStringBuilder.Builder().add(UTF8_CURLY_BRACKET_LEFT);
	}

	/**
	 * Converting json {"imageProcessor":["POST","callBack"]} to PathMethodDTO
	 *
	 * @param json
	 *            - string
	 * @return List of elements
	 */
	public static PathMethodDTO getPathMethodByJson(String json) {

		Pattern pattern = Pattern.compile("\\{\\s*\"(\\/?\\w.*)?\"\\s*:\\s*\\[(.*)\\]\\s*\\}");
		Matcher matcher = pattern.matcher(json);
		return matcher.find() ? extractPathMethodByMatcher(matcher) : null;
	}

	public static String getJsonByPathMethodList(List<PathMethodDTO> pathMethodList) {
		//@formatter:off
		return JsonElementStringBuilder.Builder().add(UTF8_SQUARE_BRACKET_LEFT)
				.add(pathMethodList.stream()
						.map(JsonUtil::getJsonByPathMethod)
						.collect(Collectors.joining(UTF8_COMMA)))
				.add(UTF8_SQUARE_BRACKET_RIGHT)
				.build();
		//@formatter:on
	}

	/**
	 * Create desired Json String: {"imageProcessor":["POST","callBack"]}
	 *
	 * @param pathMethod
	 *            valid method
	 * @return jsonString
	 */
	public static String getJsonByPathMethod(PathMethodDTO pathMethod) {
		JsonElementStringBuilder builder = JsonElementStringBuilder.Builder().add(UTF8_CURLY_BRACKET_LEFT)
				.addQuotationWithDelimiter(UTF8_COLON, pathMethod.getPath()).add(UTF8_SQUARE_BRACKET_LEFT);
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
	public static List<PathMethodDTO> convertJsonToPathMethodList(String json) {

		Pattern patternObjFromArray = Pattern.compile(PATTERN_OBJ_FROM_ARRAY);
		Matcher matcher = patternObjFromArray.matcher(json);

		//@formatter:off
		return matcher.find() ? Stream.of(matcher.group(1).split(DELIMITER_JSON_OBJECTS))
				.map(JsonUtil::getPathMethodByJson)
				.distinct()
				.collect(Collectors.toList()) : new ArrayList<>();
		//@formatter:on
	}

	public static List<ResponseUnitDTO> convertJsonToResponseUnitList(String json) {
		List<ResponseUnitDTO> result = new ArrayList<>();
		String array = json.replace(Utf8Constant.UTF8_SQUARE_BRACKET_LEFT, StringConstants.EMPTY).replace(Utf8Constant.UTF8_SQUARE_BRACKET_RIGHT,
				StringConstants.EMPTY);
		final String[] parts = array.replaceAll("\\{\\s*\"|\"\\s*\\}", StringConstants.EMPTY)
				.split("\"?(\"?\\s*:\\s*\"?|\\s*,\\s*)\"?");
		for (int i = 0; i < parts.length; i = i + 4) {
			result.add(new ResponseUnitDTO(parts[i + 1], LifecycleState.valueOf(parts[i + 3])));
		}
		return result;

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

	public static String getArraysByMethodList(List<String> list) {
		return JsonElementStringBuilder.Builder()
				.add(Utf8Constant.UTF8_SQUARE_BRACKET_LEFT)
				.add(list.stream().map(m -> JsonElementStringBuilder.Builder()
								.add(Utf8Constant.UTF8_CURLY_BRACKET_LEFT)
								.addQuotationWithDelimiter(Utf8Constant.UTF8_COLON, "type")
								.addQuotationWithDelimiter(Utf8Constant.UTF8_COMMA, m)
								.add(Utf8Constant.UTF8_CURLY_BRACKET_RIGHT)
								.build())
						.collect(Collectors.joining(Utf8Constant.UTF8_COMMA)))
				.add(Utf8Constant.UTF8_SQUARE_BRACKET_RIGHT)
				.build();
	}

	// Private Methods
	private static boolean checkPrimitiveOrWrapper(Class<?> clazz) {
		return WITHOUT_QUOTATION_TYPES.contains(clazz);
	}

	private static boolean checkString(Class<?> clazz) {
		return QUOTATION_TYPES.contains(clazz);
	}

	private static PathMethodDTO extractPathMethodByMatcher(Matcher matcher) {
		String[] propertiesValues = matcher.group(2).replaceAll("[\"\\[\\]]", StringConstants.EMPTY).split(UTF8_COMMA);
		final String pathText = matcher.group(1);

		final String path = pathText == null || pathText.isEmpty() ? Utf8Constant.UTF8_SOLIDUS : pathText.trim();

		return new PathMethodDTO(path.isEmpty() ? UTF8_SOLIDUS : path, HttpMethod.getByName(propertiesValues[0].trim()),
				propertiesValues.length > 1 ? Collections.singletonList(propertiesValues[1].trim()) : null);
	}

}
