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
import com.robo4j.socket.http.dto.ClassGetSetDTO;
import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.socket.http.dto.ResponseAttributeDTO;
import com.robo4j.socket.http.dto.ResponseAttributeListDTO;
import com.robo4j.socket.http.json.JsonDocument;
import com.robo4j.socket.http.json.JsonReader;
import com.robo4j.socket.http.json.JsonTypeAdapter;
import com.robo4j.util.Utf8Constant;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.robo4j.util.Utf8Constant.DEFAULT_ENCODING;
import static com.robo4j.util.Utf8Constant.UTF8_QUOTATION_MARK;
import static com.robo4j.util.Utf8Constant.UTF8_SQUARE_BRACKET_RIGHT;

/**
 *
 * Json related utilities
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class JsonUtil {

	public static final Set<Class<?>> QUOTATION_TYPES = Stream.of(String.class).collect(Collectors.toSet());
	static final Set<Class<?>> WITHOUT_QUOTATION_TYPES = Stream.of(boolean.class, int.class, short.class, byte.class,
			long.class, double.class, float.class, char.class, Boolean.class, Integer.class, Short.class, Byte.class,
			Long.class, Double.class, Float.class, Character.class).collect(Collectors.toSet());

	/**
	 *
	 * @param clazz
	 *            desired class
	 * @param configurationJson
	 *            json string
	 * @param <T>
	 *            desired collection type
	 * @return collection of type T
	 */
	public static <T> List<T> readPathConfig(Class<T> clazz, String configurationJson) {
		if (configurationJson == null || configurationJson.isEmpty()) {
			return Collections.emptyList();
		}
		final JsonDocument document = JsonUtil.parseJsonByClass(configurationJson);

		//@formatter:off
		return document.getArray().stream().map(JsonDocument.class::cast)
				.map(e -> ReflectUtils.createInstanceByClazzAndDescriptorAndJsonDocument(clazz, e))
				.collect(Collectors.toCollection(LinkedList::new));
		//@formatter:on
	}

	/**
	 * parse json string
	 *
	 * @param json
	 *            string input
	 * @return JsonDocument of the string input
	 */
	public static JsonDocument parseJsonByClass(String json) {
		final JsonReader jsonReader = new JsonReader(json);
		return jsonReader.read();
	}

	/**
	 *
	 * @param array
	 *            input byte array
	 * @return Base64 encoded string
	 */
	public static String toBase64String(byte[] array) {
		try {
			return new String(Base64.getEncoder().encode(array), DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new HttpException("byte array", e);
		}
	}

	/**
	 * convert json array of List of specific class instances
	 *
	 * @param clazz
	 *            desired class*
	 * @param json
	 *            array of json element of
	 * @param <T>
	 *            class type
	 * @return list of short unit response descriptions
	 */
	public static <T> List<T> jsonToList(Class<T> clazz, String json) {
		final JsonDocument document = toJsonDocument(json);
		return document.getArray().stream().map(JsonDocument.class::cast)
				.map(e -> ReflectUtils.createInstanceByClazzAndDescriptorAndJsonDocument(clazz, e))
				.collect(Collectors.toList());
	}

	/**
	 * Converting json {"imageProcessor":["POST","callBack"]} to HttpPathMethodDTO
	 *
	 * @param json
	 *            - string
	 * @return List of elements
	 */
	public static HttpPathMethodDTO getPathMethodByJson(String json) {
		final JsonDocument document = toJsonDocument(json);
		return ReflectUtils.createInstanceByClazzAndDescriptorAndJsonDocument(HttpPathMethodDTO.class, document);
	}

	public static String getJsonByPathMethodList(List<HttpPathMethodDTO> pathMethodList) {
		final JsonElementStringBuilder builder = JsonElementStringBuilder.Builder()
				.add(Utf8Constant.UTF8_SQUARE_BRACKET_LEFT);
		if (!pathMethodList.isEmpty()) {
			//@formatter:off
			builder.add(pathMethodList.stream()
					.map(ReflectUtils::createJson)
					.collect(Collectors.joining(Utf8Constant.UTF8_COMMA)));
			//@formatter:on
		}
		return builder.add(UTF8_SQUARE_BRACKET_RIGHT).build();
	}

	/**
	 * example : [{"roboUnit":"imageController",
	 * "method":"POST","callbacks":["callbackPOSTController"]},
	 *
	 * @param clazz
	 *            class of type T
	 * @param json
	 *            targetUnit json
	 * @param <T>
	 *            class type
	 * @return extracted List
	 */
	public static <T> List<T> toListFromJsonArray(Class<T> clazz, String json) {
		final JsonDocument document = toJsonDocument(json);
		//@formatter:off
		return document.getArray().stream()
				.map(JsonDocument.class::cast)
				.map(e -> ReflectUtils.createInstanceByClazzAndDescriptorAndJsonDocument(clazz, e))
				.collect(Collectors.toCollection(LinkedList::new));
		//@formatter:on
	}

	public static String toJsonMap(Map<String, String> map) {
		final JsonElementStringBuilder builder = JsonElementStringBuilder.Builder()
				.add(Utf8Constant.UTF8_CURLY_BRACKET_LEFT);
		if (!map.isEmpty()) {
			builder.add(map.entrySet().stream()
					.map(entry -> new StringBuilder().append(UTF8_QUOTATION_MARK).append(entry.getKey())
							.append(UTF8_QUOTATION_MARK).append(Utf8Constant.UTF8_COLON).append(UTF8_QUOTATION_MARK)
							.append(entry.getValue()).append(UTF8_QUOTATION_MARK))
					.collect(Collectors.joining(Utf8Constant.UTF8_COMMA)));
		}

		return builder.add(Utf8Constant.UTF8_CURLY_BRACKET_RIGHT).build();
	}

	/**
	 *
	 * @param list
	 *            list of type
	 * @param <T>
	 *            desired type
	 * @return json array
	 */
	public static <T> String toJsonArray(List<T> list) {
		return JsonElementStringBuilder.Builder().add(Utf8Constant.UTF8_SQUARE_BRACKET_LEFT)
				.add(list.stream().map(ReflectUtils::createJson).collect(Collectors.joining(Utf8Constant.UTF8_COMMA)))
				.add(Utf8Constant.UTF8_SQUARE_BRACKET_RIGHT).build();
	}

	public static <T> String toJsonArrayServer(List<T> list) {
		return JsonElementStringBuilder.Builder().add(Utf8Constant.UTF8_SQUARE_BRACKET_LEFT)
				.add(list.stream().map(e -> {
					if(e instanceof ResponseAttributeDTO){
						ResponseAttributeDTO ra = (ResponseAttributeDTO)e;
						switch (ra.getType()){
							case "java.util.ArrayList":
								List<HttpPathMethodDTO> tmpList = JsonUtil.readPathConfig(HttpPathMethodDTO.class, ra.getValue());
								ResponseAttributeListDTO tmpAttr = new ResponseAttributeListDTO();
								tmpAttr.setId(ra.getId());
								tmpAttr.setType(ra.getType());
								tmpAttr.setValue(tmpList);
								return ReflectUtils.createJson(tmpAttr);
							default:
								return ReflectUtils.createJson(e);
						}
					} else {
						return ReflectUtils.createJson(e);
					}
				}).collect(Collectors.joining(Utf8Constant.UTF8_COMMA)))
				.add(Utf8Constant.UTF8_SQUARE_BRACKET_RIGHT).build();
	}

	public static <T> String toJson(Map<String, ClassGetSetDTO> descriptorMap, T obj) {
		final JsonElementStringBuilder builder = JsonElementStringBuilder.Builder()
				.add(Utf8Constant.UTF8_CURLY_BRACKET_LEFT);
		builder.add(descriptorMap.entrySet().stream().map(entry -> {
			StringBuilder sb = new StringBuilder();
			try {
				Object val = entry.getValue().getGetMethod().invoke(obj);
				if (val == null) {
					return null;
				} else {
					TypeMapper typeMapper = TypeMapper.getBySource(val.getClass());
					JsonTypeAdapter adapter = typeMapper == null ? ReflectUtils.getJsonTypeAdapter(val.getClass())
							: typeMapper.getAdapter();
					return sb.append(Utf8Constant.UTF8_QUOTATION_MARK).append(entry.getKey())
							.append(Utf8Constant.UTF8_QUOTATION_MARK).append(Utf8Constant.UTF8_COLON)
							.append(adapter.adapt(val)).toString();
				}
			} catch (Exception e) {
				throw new RoboReflectException("adapter: " + descriptorMap + " sb: " + sb.toString(), e);
			}
		}).filter(Objects::nonNull).collect(Collectors.joining(Utf8Constant.UTF8_COMMA)));
		builder.add(Utf8Constant.UTF8_CURLY_BRACKET_RIGHT);
		return builder.build();
	}

	private static JsonDocument toJsonDocument(String json) {
		final JsonReader jsonReader = new JsonReader(json);
		return jsonReader.read();
	}
}
