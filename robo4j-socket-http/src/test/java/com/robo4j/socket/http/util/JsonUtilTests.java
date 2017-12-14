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

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static com.robo4j.util.Utf8Constant.UTF8_COLON;
import static com.robo4j.util.Utf8Constant.UTF8_COMMA;
import static com.robo4j.util.Utf8Constant.UTF8_CURLY_BRACKET_LEFT;
import static com.robo4j.util.Utf8Constant.UTF8_CURLY_BRACKET_RIGHT;

/**
 * Test json utils
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class JsonUtilTests {

	@Test
	public void getMapByJson() {
		String imageProcessorName = "imageProcessor";
		String configurationProcessorName = "configurationProcessor";
		String json = "{\"" + imageProcessorName + "\":\"POST\",\"" + configurationProcessorName + "\":\"POST\"}";
		final Map<String, Object> resultMap = JsonUtil.getMapByJson(json);

		Assert.assertNotNull(resultMap);
		Assert.assertTrue(resultMap.containsKey(imageProcessorName));
		Assert.assertTrue(resultMap.containsKey(configurationProcessorName));
	}

	@Test
	public void getMapByJsonStringTest(){
		String key_number = "number";
		String key_text = "text ";
		String key_active = "active";
		int number_value = 40;
		String text_value = "  some text";
		boolean active_value = true;

		String jsonText = "{\""+ key_number +"\" :  "+ number_value  +" , \""+ key_text +"\" :   \""+ text_value +"\", \""+ key_active +"\" :  "+ active_value +" }";

		final Map<String, Object> map = JsonUtil.getMapByJson(jsonText);
		Assert.assertTrue(!map.isEmpty());
		Assert.assertTrue(Integer.valueOf(map.get(key_number).toString()).equals(number_value));
		Assert.assertTrue(map.get(key_text.trim()).toString().equals(text_value));
		Assert.assertTrue(Boolean.valueOf(map.get(key_active).toString()).equals(active_value));
		System.out.println("MAP: " + map);
	}

	@Test
	public void jsonToMapTest() {
		String key_width = "width";
		String key_height = "height";
		String key_message = "message";
		String key_text = "text";
		String key_active = "active";
		int width = 600;
		int height = 800;
		String message = "this is a message";
		String text = "more text";
		Boolean active = true;

		String jsonNumbers = getInitJsonBuilder().addQuotationWithDelimiter(UTF8_COLON, key_width)
				.addWithDelimiter(UTF8_COMMA, width).addQuotationWithDelimiter(UTF8_COLON, key_height).add(height)
				.add(UTF8_CURLY_BRACKET_RIGHT).build();

		String jsonStrings = getInitJsonBuilder().addQuotationWithDelimiter(UTF8_COLON, key_message)
				.addQuotationWithDelimiter(UTF8_COMMA, message).addQuotationWithDelimiter(UTF8_COLON, key_text)
				.addQuotation(text).add(UTF8_CURLY_BRACKET_RIGHT).build();

		String jsonStringNumberBooleanMixed = getInitJsonBuilder().addQuotationWithDelimiter(UTF8_COLON, key_width)
				.addWithDelimiter(UTF8_COMMA, width).addQuotationWithDelimiter(UTF8_COLON, key_height)
				.addWithDelimiter(UTF8_COMMA, height).addQuotationWithDelimiter(UTF8_COLON, key_text)
				.addQuotationWithDelimiter(UTF8_COMMA, text).addQuotationWithDelimiter(UTF8_COLON, key_active)
				.add(active).add(UTF8_CURLY_BRACKET_RIGHT).build();

		final Map<String, Object> resultNumbersMap = JsonUtil.getMapByJson(jsonNumbers);
		final Map<String, Object> resultStringsMap = JsonUtil.getMapByJson(jsonStrings);
		final Map<String, Object> resultStringNumberBooleanMap = JsonUtil.getMapByJson(jsonStringNumberBooleanMixed);

		Assert.assertTrue(!resultNumbersMap.isEmpty());
		Assert.assertTrue(getIntegerByObject(resultNumbersMap.get(key_width)).equals(width));
		Assert.assertTrue(getIntegerByObject(resultNumbersMap.get(key_height)).equals(height));

		Assert.assertTrue(!resultStringsMap.isEmpty());
		Assert.assertTrue(resultStringsMap.get(key_message).equals(message));
		Assert.assertTrue(resultStringsMap.get(key_text).equals(text));

		Assert.assertTrue(!resultStringNumberBooleanMap.isEmpty());
		Assert.assertTrue(getIntegerByObject(resultStringNumberBooleanMap.get(key_width)).equals(width));
		Assert.assertTrue(getIntegerByObject(resultStringNumberBooleanMap.get(key_height)).equals(height));
		Assert.assertTrue(resultStringNumberBooleanMap.get(key_text).equals(text));
		Assert.assertTrue(getBooleanByObject(resultStringNumberBooleanMap.get(key_active)).equals(active));

	}

	private JsonElementStringBuilder getInitJsonBuilder() {
		return JsonElementStringBuilder.Builder().add(UTF8_CURLY_BRACKET_LEFT);
	}

	private Integer getIntegerByObject(Object obj) {
		return Integer.valueOf(obj.toString());
	}

	private Boolean getBooleanByObject(Object obj) {
		return Boolean.valueOf(obj.toString());
	}
}
