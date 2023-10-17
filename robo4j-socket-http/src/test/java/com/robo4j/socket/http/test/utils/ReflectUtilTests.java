/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.test.utils;

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.socket.http.dto.PathAttributeDTO;
import com.robo4j.socket.http.dto.PathAttributeListDTO;
import com.robo4j.socket.http.dto.ResponseAttributeListDTO;
import com.robo4j.socket.http.test.units.config.enums.TestCommandEnum;
import com.robo4j.socket.http.util.ReflectUtils;
import com.robo4j.socket.http.util.RoboReflectException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class ReflectUtilTests {

	@Test
	void objectWithEnumToJson() {
		final String expectedJson = "{\"command\":\"MOVE\",\"desc\":\"some description\"}";
		TestCommand command = new TestCommand();
		command.setCommand(TestCommandEnum.MOVE);
		command.setDesc("some description");

		final String result = ReflectUtils.createJson(command);

		System.out.println("result: " + result);
		assertNotNull(result);
		assertEquals(expectedJson, result);

	}

	@Test
	void objectWithEnumListToJson() {

		final String expectedJson = "{\"commands\":[\"MOVE\",\"STOP\",\"BACK\"],\"desc\":\"commands description\"}";
		TestCommandList commands = new TestCommandList();
		commands.setCommands(Arrays.asList(TestCommandEnum.MOVE, TestCommandEnum.STOP, TestCommandEnum.BACK));
		commands.setDesc("commands description");

		final String result = ReflectUtils.createJson(commands);
		System.out.println("result: " + result);

		assertNotNull(result);
		assertEquals(expectedJson, result);

	}

	@Test
	void pathAttributesListToJsonTest() {
		final String expectedJson = "{\"attributes\":[{\"name\":\"name\",\"value\":\"java.lang.String\"},{\"name\":\"values\",\"value\":\"java.util.HashMap\"}]}";
		PathAttributeListDTO attributes = new PathAttributeListDTO();
		attributes.addAttribute(new PathAttributeDTO("name", "java.lang.String"));
		attributes.addAttribute(new PathAttributeDTO("values", "java.util.HashMap"));

		String result = ReflectUtils.createJson(attributes);
		System.out.println("result:" + result);
		assertEquals(expectedJson, result);
	}

	@Test
	void serverAttributesResponse(){
		ResponseAttributeListDTO tmpAttr = new ResponseAttributeListDTO();

		tmpAttr.setType("Type");
		tmpAttr.setId("ID");
		//String roboUnit, HttpMethod method, List<String> callbacks
		tmpAttr.addValue(new HttpPathMethodDTO("testUnit", HttpMethod.GET, Collections.singletonList("test")));
		String result = ReflectUtils.createJson(tmpAttr);
		System.out.println("result:" + result);

	}

	@Test
    void complexObjectToJsonTest(){
        String expectedJson = "{\"name\":\"object\",\"value\":42,\"textList\":[\"one\",\"two\"],\"dictionary\":{\"one\":\"one1\",\"two\":\"two2\"},\"attributes\":{\"one\":{\"name\":\"name\",\"value\":\"test name\"},\"two\":{\"name\":\"value\",\"value\":\"42\"}}}";
        TestListMapValues obj = new TestListMapValues();
        obj.setName("object");
        obj.setValue(42);
        obj.setTextList(Arrays.asList("one", "two"));
        Map<String, String> textMap = new HashMap<>();
        textMap.put("one", "one1");
        textMap.put("two", "two2");
        obj.setDictionary(textMap);


        Map<String, PathAttributeDTO> attributes = new HashMap<>();
        attributes.put("one", new PathAttributeDTO("name", "test name"));
        attributes.put("two", new PathAttributeDTO("value", "42"));
        obj.setAttributes(attributes);

        String result = ReflectUtils.createJson(obj);
        System.out.println("result:"+result);
        assertEquals(expectedJson, result);
    }

    @Test
    void createJsonByStringListCollectionTest() {
		String expectedJson = "[\"One\",\"Two\"]";
        List<String> list = new ArrayList<>();
        list.add("One");
        list.add("Two");

        String result = ReflectUtils.createJson(list);
        System.out.println("result: " + result);
        assertEquals(expectedJson, result);

    }

	@Test
	void createJsonByNumberListCollectionTest() {
		String expectedJson = "[1,2]";
		List<Integer> list = new ArrayList<>();
		list.add(1);
		list.add(2);

		String result = ReflectUtils.createJson(list);
		System.out.println("result: " + result);
		assertEquals(expectedJson, result);

	}

	@Test
	void createJsonByObjectListCollectionTest(){

		Throwable exception = assertThrows(RoboReflectException.class, () -> {
			List<PathAttributeDTO> list = new ArrayList<>();
			list.add(new PathAttributeDTO("one", "1"));
			list.add(new PathAttributeDTO("two", "2"));
			ReflectUtils.createJson(list);
		});

		assertTrue(exception.getMessage().startsWith("object getter value"));
	}

	@Test
	void createJsonByStringMapCollection() {
		String expectedString = "{\"one\":\"1\",\"two\":\"2\"}";
		Map<String, String> map = new HashMap<>();
		map.put("one", "1");
		map.put("two", "2");

		String result = ReflectUtils.createJson(map);
		System.out.println("result: " + result);
		assertEquals(expectedString, result);

	}

	@Test
	void createJsonByNumberMapCollection() {
		String expectedString = "{\"one\":1,\"two\":2}";
		Map<String, Integer> map = new HashMap<>();
		map.put("one", 1);
		map.put("two", 2);

		String result = ReflectUtils.createJson(map);
		System.out.println("result: " + result);
		assertEquals(expectedString, result);
	}


	@Test
	void createJsonByObjectMapCollection(){

		Throwable exception = assertThrows(RoboReflectException.class, () -> {
			Map<String, PathAttributeDTO> map = new HashMap<>();
			map.put("one", new PathAttributeDTO("one1", "1"));
			map.put("two", new PathAttributeDTO("two2", "2"));
			ReflectUtils.createJson(map);
		});

		assertTrue(exception.getMessage().startsWith("object getter value"));
	}

}
