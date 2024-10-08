/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class ReflectUtilTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectUtilTests.class);

    @Test
    void objectWithEnumToJson() {
        final var expectedJson = "{\"command\":\"MOVE\",\"desc\":\"some description\"}";
        var command = new TestCommand();
        command.setCommand(TestCommandEnum.MOVE);
        command.setDesc("some description");

        var result = ReflectUtils.createJson(command);

        printInfo(result);
        assertNotNull(result);
        assertEquals(expectedJson, result);
    }

    @Test
    void objectWithEnumListToJson() {

        final var expectedJson = "{\"commands\":[\"MOVE\",\"STOP\",\"BACK\"],\"desc\":\"commands description\"}";
        var commands = new TestCommandList();
        commands.setCommands(Arrays.asList(TestCommandEnum.MOVE, TestCommandEnum.STOP, TestCommandEnum.BACK));
        commands.setDesc("commands description");

        final String result = ReflectUtils.createJson(commands);

        printInfo(result);
        assertNotNull(result);
        assertEquals(expectedJson, result);

    }


    @Test
    void pathAttributesListToJsonTest() {
        final var expectedJson = "{\"attributes\":[{\"name\":\"name\",\"value\":\"java.lang.String\"},{\"name\":\"values\",\"value\":\"java.util.HashMap\"}]}";
        var attributes = new PathAttributeListDTO();
        attributes.addAttribute(new PathAttributeDTO("name", "java.lang.String"));
        attributes.addAttribute(new PathAttributeDTO("values", "java.util.HashMap"));

        String result = ReflectUtils.createJson(attributes);

        printInfo(result);
        assertEquals(expectedJson, result);
    }

    @Test
    void serverAttributesResponse() {
        var tmpAttr = new ResponseAttributeListDTO();
        tmpAttr.setType("Type");
        tmpAttr.setId("ID");
        //String roboUnit, HttpMethod method, List<String> callbacks
        tmpAttr.addValue(new HttpPathMethodDTO("testUnit", HttpMethod.GET, Collections.singletonList("test")));
        String result = ReflectUtils.createJson(tmpAttr);

        printInfo(result);
    }

    @Test
    void complexObjectToJsonTest() {
        final var expectedJson = "{\"name\":\"object\",\"value\":42,\"textList\":[\"one\",\"two\"],\"dictionary\":{\"one\":\"one1\",\"two\":\"two2\"},\"attributes\":{\"one\":{\"name\":\"name\",\"value\":\"test name\"},\"two\":{\"name\":\"value\",\"value\":\"42\"}}}";
        var obj = new TestListMapValues();
        obj.setName("object");
        obj.setValue(42);
        obj.setTextList(Arrays.asList("one", "two"));
        var textMap = new HashMap<String, String>();
        textMap.put("one", "one1");
        textMap.put("two", "two2");
        obj.setDictionary(textMap);


        var attributes = new HashMap<String, PathAttributeDTO>();
        attributes.put("one", new PathAttributeDTO("name", "test name"));
        attributes.put("two", new PathAttributeDTO("value", "42"));
        obj.setAttributes(attributes);

        var result = ReflectUtils.createJson(obj);

        printInfo(result);
        assertEquals(expectedJson, result);
    }

    @Test
    void createJsonByStringListCollectionTest() {
        final var expectedJson = "[\"One\",\"Two\"]";
        var list = new ArrayList<String>();
        list.add("One");
        list.add("Two");

        String result = ReflectUtils.createJson(list);

        printInfo(result);
        assertEquals(expectedJson, result);
    }

    @Test
    void createJsonByNumberListCollectionTest() {
        final var expectedJson = "[1,2]";
        var list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);

        var result = ReflectUtils.createJson(list);

        printInfo(result);
        assertEquals(expectedJson, result);

    }

    @Test
    void createJsonByObjectListCollectionTest() {

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
        final var expectedString = "{\"one\":\"1\",\"two\":\"2\"}";
        var map = new HashMap<String, String>();
        map.put("one", "1");
        map.put("two", "2");

        var result = ReflectUtils.createJson(map);

        printInfo(result);
        assertEquals(expectedString, result);

    }

    @Test
    void createJsonByNumberMapCollection() {
        final var expectedString = "{\"one\":1,\"two\":2}";
        var map = new HashMap<String, Integer>();
        map.put("one", 1);
        map.put("two", 2);

        var result = ReflectUtils.createJson(map);

        printInfo(result);
        assertEquals(expectedString, result);
    }


    @Test
    void createJsonByObjectMapCollection() {

        Throwable exception = assertThrows(RoboReflectException.class, () -> {
            Map<String, PathAttributeDTO> map = new HashMap<>();
            map.put("one", new PathAttributeDTO("one1", "1"));
            map.put("two", new PathAttributeDTO("two2", "2"));
            ReflectUtils.createJson(map);
        });

        assertTrue(exception.getMessage().startsWith("object getter value"));
    }

    private static void printInfo(String result) {
        LOGGER.info("result: {}", result);
    }

}
