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
import java.util.Collections;
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
		final String expectedResult = "[{\"/units/imageController\":[\"POST\",[\"callbackPOSTController\"]]}," +
				"{\"/units/imageController\":[\"GET\",[\"callbackGETController\"]]}," +
				"{\"/units/cameraController\":[\"POST\",[\"callbackPOSTController\"]]}," +
				"{\"/units/cameraController\":[\"GET\",[\"callbackGETController\"]]}," +
				"{\"/units/emptyController\":[\"GET\"]}]";
		List<PathMethodDTO> pathMethodList = PropertyListBuilder.Builder()
				.add(createPathMethodDTO("/units/imageController", "POST","callbackPOSTController"))
				.add(createPathMethodDTO("/units/imageController", "GET","callbackGETController"))
				.add(createPathMethodDTO("/units/cameraController", "POST","callbackPOSTController"))
				.add(createPathMethodDTO("/units/cameraController", "GET","callbackGETController"))
				.add(createPathMethodDTO("/units/emptyController", "GET"))
				.build();


		String jsonArray = JsonUtil.getJsonByPathMethodList(pathMethodList);
		Assert.assertNotNull(jsonArray);
		Assert.assertTrue(expectedResult.equals(jsonArray));
	}



	@Test
	public void parseJsonArrayPathMethodToList(){
		final int observedElement = 2;
		final String jsonArray = "[{\"/units/imageController\":[\"POST\",[\"callbackPOSTController\"]]}," +
				"{\"/units/imageController\":[\"GET\",[\"callbackGETController\"]]}," +
				"{\"/units/cameraController\":[\"POST\",[\"callbackPOSTController\"]]}," +
				"{\"/units/cameraController\":[\"POST\",[\"callbackPOSTController\"]]}," +
				"{\"/units/cameraController\":[\"GET\",[\"callbackGETController\"]]}," +
				"{\"/units/emptyController\":[\"GET\"]}]";

		PathMethodDTO duplicate = new PathMethodDTO("/units/cameraController", HttpMethod.POST, Collections.singletonList("callbackPOSTController"));
		List<PathMethodDTO> result = JsonUtil.convertJsonToPathMethodList(jsonArray);

		long duplicatesNumber = result.stream().filter(e -> e.equals(duplicate)).count();

		Assert.assertNotNull(result);
		Assert.assertTrue(result.size() == 5);
		Assert.assertTrue(result.get(observedElement).getPath().equals("/units/cameraController"));
		Assert.assertTrue(result.get(observedElement).getMethod().equals(HttpMethod.POST));
		Assert.assertTrue(result.get(observedElement).getCallbacks().contains("callbackPOSTController"));
		Assert.assertTrue(duplicatesNumber == 1);

	}

	@Test
	public void parseJsonPathMethod(){
		List<String> jsonList = Arrays.asList(
				"{\"/units/imageController\":[\"POST\",[\"callbackPOSTController\"]]}",
				"{\"/units/imageController\":[\"POST\",[\"callbackPOSTController\"]]}",
				"{ \"/units/imageController\" : [\"POST\",[\"callbackPOSTController\"]]}",
				"{ \"/units/imageController\" : [\"POST\",[\"callbackPOSTController\"]]}"
		);

		jsonList.forEach(json -> {
			PathMethodDTO pathMethod = JsonUtil.getPathMethodByJson(json);
			Assert.assertNotNull(json, pathMethod);
			Assert.assertTrue(json, pathMethod.getPath().equals("/units/imageController"));
			Assert.assertTrue(json, pathMethod.getMethod().equals(HttpMethod.POST));
			Assert.assertTrue(json, pathMethod.getCallbacks().contains("callbackPOSTController"));
		});
	}

	@Test
	public void parseFullPathMethod() {
		List<String> jsonList = Arrays.asList(
				"{\"\":[\"POST\",[\"callbackPOSTController\"]]}",
				"{\"imageController\":[\"POST\",[\"callbackPOSTController\"]]}",
				"{\"/imageController\":[\"POST\",[\"callbackPOSTController\"]]}",
				"{\"/units/imageController\":[\"POST\",[\"callbackPOSTController\"]]}"
		);

		jsonList.forEach(json -> {
			PathMethodDTO pathMethod = JsonUtil.getPathMethodByJson(json);
			Assert.assertNotNull(json, pathMethod);
			Assert.assertNotNull(pathMethod.getPath());
			Assert.assertTrue(!pathMethod.getPath().isEmpty());
		});
	}

	private PathMethodDTO createPathMethodDTO(String... args) {
		List<String> properties = args.length > 2 ? Collections.singletonList(args[2]) : null;
		return new PathMethodDTO(args[0], HttpMethod.getByName(args[1]), properties);
	}

}
