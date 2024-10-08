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
import com.robo4j.socket.http.test.units.config.PropertyListBuilder;
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.ReflectUtils;
import com.robo4j.util.StringConstants;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.robo4j.socket.http.util.HttpPathUtils.ATTRIBUTES_PATH_VALUE;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpPathUtilTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpPathUtilTests.class);

    @SuppressWarnings("unchecked")
    @Test
    public void convertPathMethodToJson() {

        //@formatter:off
		final var expectedResult = "[{\"roboUnit\":\"imageController\",\"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}," +
				"{\"roboUnit\":\"imageController\",\"method\":\"GET\",\"callbacks\":[\"callbackGETController\"]}," +
				"{\"roboUnit\":\"cameraController\",\"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}," +
				"{\"roboUnit\":\"cameraController\",\"method\":\"GET\",\"callbacks\":[\"callbackGETController\"]}," +
				"{\"roboUnit\":\"emptyController\",\"method\":\"GET\"}]";
		final var pathMethodList = PropertyListBuilder.Builder()
				.add(createPathMethodDTO("imageController", "POST","callbackPOSTController"))
				.add(createPathMethodDTO("imageController", "GET","callbackGETController"))
				.add(createPathMethodDTO("cameraController", "POST","callbackPOSTController"))
				.add(createPathMethodDTO("cameraController", "GET","callbackGETController"))
				.add(createPathMethodDTO("emptyController", "GET"))
				.build();

		var result = JsonUtil.getJsonByPathMethodList(pathMethodList);

		printInfo(result);

		assertNotNull(result);
		assertEquals(expectedResult, result);
	}

	@Test
	void parseJsonArrayPathMethodToList(){
		final var observedElement = 2;
		final var jsonArray = "[{\"roboUnit\":\"imageController\", \"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}," +
				"{\"roboUnit\":\"imageController\",\"method\":\"GET\",\"callbacks\":[\"callbackGETController\"]}," +
				"{\"roboUnit\":\"cameraController\",\"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}," +
				"{\"roboUnit\":\"cameraController\",\"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}," +
				"{\"roboUnit\":\"cameraController\",\"method\":\"GET\",\"callbacks\":[\"callbackGETController\"]}," +
				"{\"roboUnit\":\"emptyController\",\"method\":\"GET\"}]";

		var duplicate = new HttpPathMethodDTO("cameraController", HttpMethod.POST, Collections.singletonList("callbackPOSTController"));
		var result = JsonUtil.toListFromJsonArray(HttpPathMethodDTO.class, jsonArray);

		printInfo(result);

		assertNotNull(result);
		assertEquals(6, result.size());
		assertEquals("cameraController", result.get(observedElement).getRoboUnit());
		assertEquals(HttpMethod.POST, result.get(observedElement).getMethod());
		assertTrue(result.get(observedElement).getCallbacks().contains("callbackPOSTController"));
		assertTrue(result.contains(duplicate));

	}

	@Test
	void parseJsonPathMethod(){
		final var jsonList = Arrays.asList(
				"{\"roboUnit\":\"imageController\", \"method\":\"POST\", \"callbacks\":[\"callbackPOSTController\"]}",
				"{\"roboUnit\":\"imageController\", \"method\" : \"POST\", \"callbacks\" : [ \"callbackPOSTController\" ]}",
				"{ \"roboUnit\": \"imageController\", \"method\": \"POST\", \"callbacks\" :[\"callbackPOSTController\"] }"
		);

		jsonList.forEach(json -> {
			HttpPathMethodDTO pathMethod = JsonUtil.getPathMethodByJson(json);
			assertNotNull(pathMethod, json);
			assertEquals("imageController", pathMethod.getRoboUnit(), json);
			assertEquals(HttpMethod.POST, pathMethod.getMethod(), json);
			assertTrue(pathMethod.getCallbacks().contains("callbackPOSTController"), json);
		});
	}

	@Test
	void parseFullPathMethod() {
		final var jsonList = Arrays.asList(
				"{\"roboUnit\":\"\", \"method\":\"POST\", \"callbacks\":[\"callbackPOSTController\"]}",
				"{\"roboUnit\":\"imageController\", \"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}",
				"{\"roboUnit\":\"imageController\", \"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}",
				"{\"roboUnit\":\"imageController\", \"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}"
		);

		jsonList.forEach(json -> {
			HttpPathMethodDTO pathMethod = JsonUtil.getPathMethodByJson(json);
			printInfo(pathMethod);
			assertNotNull(pathMethod, json);
			assertNotNull(pathMethod.getRoboUnit(), json);
			assertTrue(pathMethod.getRoboUnit().isEmpty() ?
					pathMethod.getRoboUnit().equals(StringConstants.EMPTY) : pathMethod.getRoboUnit().equals("imageController"));
		});
	}

	@Test
	void parseGetRequestWithAttributes(){
		var path = "/units/controller?attributes=number,counter";
		var expectedMap = new HashMap<String, Set<String>>();
		var expectedAttributesValues = new HashSet<String>();
		var attributeName = "attributes";

		expectedAttributesValues.add("number");
		expectedAttributesValues.add("counter");
		expectedMap.put(ATTRIBUTES_PATH_VALUE, expectedAttributesValues);

		HttpPathUtils.extractAttributesByPath(path);
		var attributeMap = HttpPathUtils.extractAttributesByPath(path);

		assertArrayEquals(attributeMap.get(attributeName).toArray(), expectedMap.get(attributeName).toArray());
	}

	@Test
	void createJsonArrayByList(){
		var attributeDTO = new PathAttributeDTO("number", "42");

		var descriptorMap = ReflectUtils.getFieldsTypeMap(PathAttributeDTO.class);

		printInfo(JsonUtil.toJson(descriptorMap, attributeDTO));

	}

	private HttpPathMethodDTO createPathMethodDTO(String... args) {
		var properties = args.length > 2 ? Collections.singletonList(args[2]) : null;
		return new HttpPathMethodDTO(args[0], HttpMethod.getByName(args[1]), properties);
	}

	private static <T> void printInfo(T result) {
		LOGGER.info("result: {}",result);
	}

}
