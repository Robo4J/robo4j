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
import com.robo4j.socket.http.dto.PathMethodDTO;
import com.robo4j.socket.http.units.test.PropertyListBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpPathUtilTests {

	@SuppressWarnings("unchecked")
	@Test
	public void convertPathMethodToJson() {

		//@formatter:off
		final String expectedResult = "[{\"imageController\":[\"POST\",\"callbackPOSTController\"]}," +
				"{\"imageController\":[\"GET\",\"callbackGETController\"]}," +
				"{\"cameraController\":[\"POST\",\"callbackPOSTController\"]}," +
				"{\"cameraController\":[\"GET\",\"callbackGETController\"]}," +
				"{\"emptyController\":[\"GET\"]}]";
		List<PathMethodDTO> pathMethodList = PropertyListBuilder.Builder()
				.add(createPathMethodDTO("imageController", "POST","callbackPOSTController"))
				.add(createPathMethodDTO("imageController", "GET","callbackGETController"))
				.add(createPathMethodDTO("cameraController", "POST","callbackPOSTController"))
				.add(createPathMethodDTO("cameraController", "GET","callbackGETController"))
				.add(createPathMethodDTO("emptyController", "GET"))
				.build();


		String jsonArray = JsonUtil.getJsonByPathMethodList(pathMethodList);
		Assert.assertNotNull(jsonArray);
		Assert.assertTrue(expectedResult.equals(jsonArray));
	}

	@Test
	public void parseJsonArrayPathMethodToList(){
		final int observedElement = 2;
		final String jsonArray = "[{\"imageController\":[\"POST\",\"callbackPOSTController\"]}," +
				"{\"imageController\":[\"GET\",\"callbackGETController\"]}," +
				"{\"cameraController\":[\"POST\",\"callbackPOSTController\"]}," +
				"{\"cameraController\":[\"POST\",\"callbackPOSTController\"]}," +
				"{\"cameraController\":[\"GET\",\"callbackGETController\"]}," +
				"{\"emptyController\":[\"GET\"]}]";

		PathMethodDTO duplicate = new PathMethodDTO("cameraController", HttpMethod.POST, "callbackPOSTController");
		List<PathMethodDTO> result = JsonUtil.convertJsonToPathMethodList(jsonArray);

		long duplicatesNumber = result.stream().filter(e -> e.equals(duplicate)).count();

		Assert.assertNotNull(result);
		Assert.assertTrue(result.size() == 5);
		Assert.assertTrue(result.get(observedElement).getPath().equals("cameraController"));
		Assert.assertTrue(result.get(observedElement).getMethod().equals(HttpMethod.POST));
		Assert.assertTrue(result.get(observedElement).getCallbackUnitName().equals("callbackPOSTController"));
		Assert.assertTrue(duplicatesNumber == 1);

	}

	@Test
	public void parseJsonPathMethod(){
		List<String> jsonList = Arrays.asList(
				"{\"imageController\":[\"POST\",\"callbackPOSTController\"]}",
				"{\"imageController\":[\"POST\",\"callbackPOSTController\"] }",
				"{ \"imageController\" : [\"POST\",\"callbackPOSTController\"] }",
				"{ \"imageController\" : [\"POST\", \"callbackPOSTController\"] }"
		);

		jsonList.forEach(json -> {
			PathMethodDTO pathMethod = JsonUtil.getPathMethodByJson(json);
			Assert.assertNotNull(json, pathMethod);
			Assert.assertTrue(json, pathMethod.getPath().equals("imageController"));
			Assert.assertTrue(json, pathMethod.getMethod().equals(HttpMethod.POST));
			Assert.assertTrue(json, pathMethod.getCallbackUnitName().equals("callbackPOSTController"));
		});
	}

	private PathMethodDTO createPathMethodDTO(String... args) {
		String properties = args.length > 2 ? args[2] : null;
		return new PathMethodDTO(args[0], HttpMethod.getByName(args[1]), properties);
	}

}
