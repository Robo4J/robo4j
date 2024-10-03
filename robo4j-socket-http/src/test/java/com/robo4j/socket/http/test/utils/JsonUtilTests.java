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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * json related utils tests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class JsonUtilTests {

	@Test
	void jsonToListTest() {

		String json = "[{\"id\":\"unit1\",\"state\":\"INITIALIZED\"}," + "{\"id\":\"unit2\",\"state\":\"STARTED\"}, "
				+ "{\"id\":\"unit3\",\"state\":\"FAILED\"}]";

		List<ResponseUnitDTO> expectedResult = Arrays.asList(new ResponseUnitDTO("unit1", LifecycleState.INITIALIZED),
				new ResponseUnitDTO("unit2", LifecycleState.STARTED),
				new ResponseUnitDTO("unit3", LifecycleState.FAILED));
		List<ResponseUnitDTO> result = JsonUtil.jsonToList(ResponseUnitDTO.class, json);

		assertNotNull(result);
		assertArrayEquals(expectedResult.toArray(), result.toArray());
	}

	@Test
	void jsonToListEmptyTest() {
		String json = "[]";
		List<ResponseUnitDTO> result = JsonUtil.jsonToList(ResponseUnitDTO.class, json);

		assertNotNull(result);
		assertArrayEquals(Collections.emptyList().toArray(), result.toArray());
	}

	@Test
	void mapToJsonTest() {
		String expectedJson = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
		Map<String, String> testMap = new HashMap<>();
		testMap.put("key1", "value1");
		testMap.put("key2", "value2");

		String result = JsonUtil.toJsonMap(testMap);
		System.out.println("result: " + result);

		assertNotNull(result);
		assertEquals(result, expectedJson);
	}

	@Test
	void mapToJsonEmptyTest() {
		String expectedJson = "{}";

		String result = JsonUtil.toJsonMap(new HashMap<>());
		System.out.println("result: " + result);

		assertNotNull(result);
		assertEquals(result, expectedJson);
	}
}
