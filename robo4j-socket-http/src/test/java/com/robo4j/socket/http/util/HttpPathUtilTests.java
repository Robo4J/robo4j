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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.socket.http.util;

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.ClientPathDTO;
import com.robo4j.socket.http.units.test.PropertyListBuilder;
import com.robo4j.util.StringConstants;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
		List<ClientPathDTO> pathMethodList = PropertyListBuilder.Builder()
				.add(createPathMethodDTO("imageController", "POST","callbackPOSTController"))
				.add(createPathMethodDTO("imageController", "GET","callbackGETController"))
				.add(createPathMethodDTO("cameraController", "POST","callbackPOSTController"))
				.add(createPathMethodDTO("cameraController", "GET","callbackGETController"))
				.add(createPathMethodDTO("emptyController", "GET"))
				.build();

		String result = JsonUtil.getJsonByPathMethodList(pathMethodList);

		System.out.println("result: " + result);

		Assert.assertNotNull(result);
		Assert.assertTrue(expectedResult.equals(result));
	}

	@Test
	public void parseJsonArrayPathMethodToList(){
		final int observedElement = 2;
		final String jsonArray = "[{\"roboUnit\":\"imageController\", \"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}," +
				"{\"roboUnit\":\"imageController\",\"method\":\"GET\",\"callbacks\":[\"callbackGETController\"]}," +
				"{\"roboUnit\":\"cameraController\",\"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}," +
				"{\"roboUnit\":\"cameraController\",\"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}," +
				"{\"roboUnit\":\"cameraController\",\"method\":\"GET\",\"callbacks\":[\"callbackGETController\"]}," +
				"{\"roboUnit\":\"emptyController\",\"method\":\"GET\"}]";

		ClientPathDTO duplicate = new ClientPathDTO("cameraController", HttpMethod.POST, Collections.singletonList("callbackPOSTController"));
		List<ClientPathDTO> result = JsonUtil.toListFromJsonArray(ClientPathDTO.class, jsonArray);

		System.out.println("result: " + result);

		Assert.assertNotNull(result);
		Assert.assertTrue(result.size() == 6);
		Assert.assertTrue(result.get(observedElement).getRoboUnit().equals("cameraController"));
		Assert.assertTrue(result.get(observedElement).getMethod().equals(HttpMethod.POST));
		Assert.assertTrue(result.get(observedElement).getCallbacks().contains("callbackPOSTController"));
		Assert.assertTrue(result.contains(duplicate));

	}

	@Test
	public void parseJsonPathMethod(){
		List<String> jsonList = Arrays.asList(
				"{\"roboUnit\":\"imageController\", \"method\":\"POST\", \"callbacks\":[\"callbackPOSTController\"]}",
				"{\"roboUnit\":\"imageController\", \"method\" : \"POST\", \"callbacks\" : [ \"callbackPOSTController\" ]}",
				"{ \"roboUnit\": \"imageController\", \"method\": \"POST\", \"callbacks\" :[\"callbackPOSTController\"] }"
		);

		jsonList.forEach(json -> {
			ClientPathDTO pathMethod = JsonUtil.getPathMethodByJson(json);
			Assert.assertNotNull(json, pathMethod);
			Assert.assertTrue(json, pathMethod.getRoboUnit().equals("imageController"));
			Assert.assertTrue(json, pathMethod.getMethod().equals(HttpMethod.POST));
			Assert.assertTrue(json, pathMethod.getCallbacks().contains("callbackPOSTController"));
		});
	}

	@Test
	public void parseFullPathMethod() {
		List<String> jsonList = Arrays.asList(
				"{\"roboUnit\":\"\", \"method\":\"POST\", \"callbacks\":[\"callbackPOSTController\"]}",
				"{\"roboUnit\":\"imageController\", \"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}",
				"{\"roboUnit\":\"imageController\", \"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}",
				"{\"roboUnit\":\"imageController\", \"method\":\"POST\",\"callbacks\":[\"callbackPOSTController\"]}"
		);

		jsonList.forEach(json -> {
			ClientPathDTO pathMethod = JsonUtil.getPathMethodByJson(json);
			System.out.println("pathMethod: " + pathMethod);
			Assert.assertNotNull(json, pathMethod);
			Assert.assertNotNull(pathMethod.getRoboUnit());
			Assert.assertTrue(pathMethod.getRoboUnit().isEmpty() ?
					pathMethod.getRoboUnit().equals(StringConstants.EMPTY) : pathMethod.getRoboUnit().equals("imageController"));
		});
	}

	private ClientPathDTO createPathMethodDTO(String... args) {
		List<String> properties = args.length > 2 ? Collections.singletonList(args[2]) : null;
		return new ClientPathDTO(args[0], HttpMethod.getByName(args[1]), properties);
	}

}
