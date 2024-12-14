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

import com.robo4j.LifecycleState;
import com.robo4j.socket.http.dto.ResponseUnitDTO;
import com.robo4j.socket.http.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * json related utils tests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class JsonUtilTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtilTests.class);

    @Test
    void jsonToListTest() {

        var json = "[{\"id\":\"unit1\",\"state\":\"INITIALIZED\"}," + "{\"id\":\"unit2\",\"state\":\"STARTED\"}, "
                + "{\"id\":\"unit3\",\"state\":\"FAILED\"}]";

        var expectedResult = Arrays.asList(new ResponseUnitDTO("unit1", LifecycleState.INITIALIZED),
                new ResponseUnitDTO("unit2", LifecycleState.STARTED),
                new ResponseUnitDTO("unit3", LifecycleState.FAILED));
        var result = JsonUtil.jsonToList(ResponseUnitDTO.class, json);

        assertNotNull(result);
        assertArrayEquals(expectedResult.toArray(), result.toArray());
    }

    @Test
    void jsonToListEmptyTest() {
        var json = "[]";
        var result = JsonUtil.jsonToList(ResponseUnitDTO.class, json);

        assertNotNull(result);
        assertArrayEquals(Collections.emptyList().toArray(), result.toArray());
    }

    @Test
    void mapToJsonTest() {
        var expectedJson = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
        var testMap = new HashMap<String, String>();
        testMap.put("key1", "value1");
        testMap.put("key2", "value2");

        var result = JsonUtil.toJsonMap(testMap);

        printInfo(result);
        assertNotNull(result);
        assertEquals(expectedJson, result);
    }

    @Test
    void mapToJsonEmptyTest() {
        var expectedJson = "{}";

        var result = JsonUtil.toJsonMap(new HashMap<>());

        printInfo(result);
        assertNotNull(result);
        assertEquals(expectedJson, result);
    }

    private static void printInfo(String result) {
        LOGGER.info("result: {}", result);
    }
}
