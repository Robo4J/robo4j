/*
 * Copyright (c) 2014-2019, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.ClassGetSetDTO;
import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.socket.http.dto.PathAttributeDTO;
import com.robo4j.socket.http.units.test.PropertyListBuilder;
import com.robo4j.util.StringConstants;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.robo4j.socket.http.util.HttpPathUtils.ATTRIBUTES_PATH_VALUE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpPathUtilTests {

	//String roboUnit;
	//	private HttpMethod method;
	//	private List<String> callbacks;

	@SuppressWarnings("unchecked")
	@Test
	public void convertPathMethodToJson() {

		//@formatter:off
		final String expectedResult = "[{\"roboUnit\":\"imageController\",\"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}," +
				"{\"roboUnit\":\"imageController\",\"method\":\"GET\",\"callbacks\":[\"callbackGETController\"]}," +
				"{\"roboUnit\":\"cameraController\",\"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}," +
				"{\"roboUnit\":\"cameraController\",\"method\":\"GET\",\"callbacks\":[\"callbackGETController\"]}," +
				"{\"roboUnit\":\"emptyController\",\"method\":\"GET\"}]";
		List<HttpPathMethodDTO> pathMethodList = PropertyListBuilder.Builder()
				.add(createPathMethodDTO("imageController", "POST","callbackPOSTController"))
				.add(createPathMethodDTO("imageController", "GET","callbackGETController"))
				.add(createPathMethodDTO("cameraController", "POST","callbackPOSTController"))
				.add(createPathMethodDTO("cameraController", "GET","callbackGETController"))
				.add(createPathMethodDTO("emptyController", "GET"))
				.build();

		String result = JsonUtil.getJsonByPathMethodList(pathMethodList);

		System.out.println("result: " + result);

		assertNotNull(result);
		assertEquals(expectedResult, result);
	}

	@Test
	void parseJsonArrayPathMethodToList(){
		final int observedElement = 2;
		final String jsonArray = "[{\"roboUnit\":\"imageController\", \"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}," +
				"{\"roboUnit\":\"imageController\",\"method\":\"GET\",\"callbacks\":[\"callbackGETController\"]}," +
				"{\"roboUnit\":\"cameraController\",\"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}," +
				"{\"roboUnit\":\"cameraController\",\"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}," +
				"{\"roboUnit\":\"cameraController\",\"method\":\"GET\",\"callbacks\":[\"callbackGETController\"]}," +
				"{\"roboUnit\":\"emptyController\",\"method\":\"GET\"}]";

		HttpPathMethodDTO duplicate = new HttpPathMethodDTO("cameraController", HttpMethod.POST, Collections.singletonList("callbackPOSTController"));
		List<HttpPathMethodDTO> result = JsonUtil.toListFromJsonArray(HttpPathMethodDTO.class, jsonArray);

		System.out.println("result: " + result);

		assertNotNull(result);
		assertEquals(6, result.size());
		assertEquals("cameraController", result.get(observedElement).getRoboUnit());
		assertEquals(HttpMethod.POST, result.get(observedElement).getMethod());
		assertTrue(result.get(observedElement).getCallbacks().contains("callbackPOSTController"));
		assertTrue(result.contains(duplicate));

	}

	@Test
	void parseJsonPathMethod(){
		List<String> jsonList = Arrays.asList(
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
		List<String> jsonList = Arrays.asList(
				"{\"roboUnit\":\"\", \"method\":\"POST\", \"callbacks\":[\"callbackPOSTController\"]}",
				"{\"roboUnit\":\"imageController\", \"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}",
				"{\"roboUnit\":\"imageController\", \"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}",
				"{\"roboUnit\":\"imageController\", \"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}"
		);

		jsonList.forEach(json -> {
			HttpPathMethodDTO pathMethod = JsonUtil.getPathMethodByJson(json);
			System.out.println("pathMethod: " + pathMethod);
			assertNotNull(pathMethod, json);
			assertNotNull(pathMethod.getRoboUnit(), json);
			assertTrue(pathMethod.getRoboUnit().isEmpty() ?
					pathMethod.getRoboUnit().equals(StringConstants.EMPTY) : pathMethod.getRoboUnit().equals("imageController"));
		});
	}

	@Test
	void parseGetRequestWithAttributes(){
		String path = "/units/controller?attributes=number,counter";
		Map<String, Set<String>> expectedMap = new HashMap<>();
		Set<String> expectedAttributesValues = new HashSet<>();
		String attributeName = "attributes";
		expectedAttributesValues.add("number");
		expectedAttributesValues.add("counter");
		expectedMap.put(ATTRIBUTES_PATH_VALUE, expectedAttributesValues);

		HttpPathUtils.extractAttributesByPath(path);
		Map<String, Set<String>> attributeMap = HttpPathUtils.extractAttributesByPath(path);
		assertArrayEquals(attributeMap.get(attributeName).toArray(), expectedMap.get(attributeName).toArray());
	}

	@Test
	void createJsonArrayByList(){

		PathAttributeDTO attributeDTO = new PathAttributeDTO("number", "42");

		Map<String, ClassGetSetDTO> descriptorMap = ReflectUtils.getFieldsTypeMap(PathAttributeDTO.class);

		System.out.println("result: " + JsonUtil.toJson(descriptorMap, attributeDTO));

	}

	private HttpPathMethodDTO createPathMethodDTO(String... args) {
		List<String> properties = args.length > 2 ? Collections.singletonList(args[2]) : null;
		return new HttpPathMethodDTO(args[0], HttpMethod.getByName(args[1]), properties);
	}

}
