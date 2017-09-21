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
import com.robo4j.socket.http.dto.ResponseUnitDTO;
import com.robo4j.socket.http.units.Constants;
import com.robo4j.util.StringConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.robo4j.socket.http.util.RoboHttpUtils.CHAR_COLON;
import static com.robo4j.socket.http.util.RoboHttpUtils.CHAR_COMMA;
import static com.robo4j.socket.http.util.RoboHttpUtils.CHAR_CURLY_BRACKET_LEFT;
import static com.robo4j.socket.http.util.RoboHttpUtils.CHAR_CURLY_BRACKET_RIGHT;
import static com.robo4j.socket.http.util.RoboHttpUtils.CHAR_QUOTATION_MARK;
import static com.robo4j.socket.http.util.RoboHttpUtils.CHAR_SQUARE_BRACKET_LEFT;
import static com.robo4j.socket.http.util.RoboHttpUtils.CHAR_SQUARE_BRACKET_RIGHT;

/**
 *
 * Simple Json util
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class JsonUtil {

	private static final Set<Class<?>> withoutQuotationTypes = Stream.of(boolean.class, int.class, short.class,
			byte.class, long.class, double.class, float.class, char.class, Boolean.class, Integer.class, Short.class,
			Byte.class, Long.class, Double.class, Float.class, Character.class).collect(Collectors.toSet());
	private static final Set<Class<?>> quoatationTypes = Stream.of(String.class).collect(Collectors.toSet());

	@SuppressWarnings(value = "unchecked")
	public static String getJsonByMap(Map<String, Object> map) {
		StringBuilder sb = new StringBuilder(Constants.UTF8_CURLY_BRACKET_LEFT);
		sb.append(map.entrySet().stream().map(e -> {
			StringBuilder sb2 = new StringBuilder(Constants.UTF8_QUOTATION_MARK).append(e.getKey())
					.append(Constants.UTF8_QUOTATION_MARK).append(Constants.UTF8_COLON);
			Class<?> clazz = e.getValue().getClass();
			if (checkPrimitiveOrWrapper(clazz)) {
				sb2.append(e.getValue());
			} else if (checkString(clazz)) {
				sb2.append(Constants.UTF8_QUOTATION_MARK).append(e.getValue()).append(Constants.UTF8_QUOTATION_MARK);
			} else if (e.getValue() instanceof Enum<?>){
				Enum<?> en = (Enum<?>)e.getValue();
				sb2.append(Constants.UTF8_QUOTATION_MARK).append(en.name()).append(Constants.UTF8_QUOTATION_MARK);
			} else if (e.getValue() instanceof Map) {
				sb2.append(getJsonByMap((Map<String, Object>) e.getValue()));
			}
			return sb2.toString();
		}).collect(Collectors.joining(Constants.UTF8_COMMA))).append(Constants.UTF8_CURLY_BRACKET_RIGHT);
		return sb.toString();
	}

	public static Map<String, Object> getMapNyJson(String json){
		final Map<String, Object> result = new HashMap<>();
		if(json == null){
			return result;
		}

		final String[] parts = json.replaceAll("^\\{\\s*\"|\"\\s*\\}$", StringConstants.EMPTY)
				.split("\"?(\"?\\s*:\\s*\"?|\\s*,\\s*)\"?");

		for (int i = 0; i < parts.length - 1; i += 2) {
			result.put(parts[i], parts[i + 1]);
		}
		return result;

	}

	public static List<ResponseUnitDTO> getListByUnitJsonArray(String json){
		List<ResponseUnitDTO> result = new ArrayList<>();
		String array = json.replace(CHAR_SQUARE_BRACKET_LEFT,StringConstants.EMPTY)
				.replace(CHAR_SQUARE_BRACKET_RIGHT, StringConstants.EMPTY);
		final String[] parts = array.replaceAll("\\{\\s*\"|\"\\s*\\}", StringConstants.EMPTY)
				.split("\"?(\"?\\s*:\\s*\"?|\\s*,\\s*)\"?");
		for(int i=0; i < parts.length; i=i+4){
			result.add(new ResponseUnitDTO(parts[i+1], LifecycleState.valueOf(parts[i+3])));
		}
		return result;

	}


	public static String getArrayByListResponseUnitDTO(List<ResponseUnitDTO> units){

		return new StringBuilder(CHAR_SQUARE_BRACKET_LEFT)
						.append(units.stream().map(u -> new StringBuilder(CHAR_CURLY_BRACKET_LEFT)
								.append(CHAR_QUOTATION_MARK).append("id").append(CHAR_QUOTATION_MARK).append(CHAR_COLON)
								.append(CHAR_QUOTATION_MARK).append(u.getId()).append(CHAR_QUOTATION_MARK).append(CHAR_COMMA)
								.append(CHAR_QUOTATION_MARK).append(u.getState().getClass().getCanonicalName()).append(CHAR_QUOTATION_MARK)
								.append(CHAR_COLON).append(CHAR_QUOTATION_MARK).append(u.getState().getLocalizedName().toUpperCase()).append(CHAR_QUOTATION_MARK)
								.append(CHAR_CURLY_BRACKET_RIGHT).toString())

								.collect(Collectors.joining(CHAR_COMMA)))
						.append(CHAR_SQUARE_BRACKET_RIGHT).toString();
	}

	public static String getArraysByMethodList(List<String> list){
		return new StringBuilder(CHAR_SQUARE_BRACKET_LEFT).append(list.stream().map(m -> new StringBuilder(CHAR_CURLY_BRACKET_LEFT)
						.append(CHAR_QUOTATION_MARK).append("type").append(CHAR_QUOTATION_MARK)
						.append(CHAR_COLON).append(CHAR_QUOTATION_MARK).append(m).append(CHAR_QUOTATION_MARK)
						.append(CHAR_CURLY_BRACKET_RIGHT).toString()).collect(Collectors.joining(CHAR_COMMA)))
				.append(CHAR_SQUARE_BRACKET_RIGHT).toString();
	}




	// Private Methods
	private static boolean checkPrimitiveOrWrapper(Class<?> clazz) {
		return withoutQuotationTypes.contains(clazz);
	}

	private static boolean checkString(Class<?> clazz) {
		return quoatationTypes.contains(clazz);
	}

}
