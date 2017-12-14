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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test json utils
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class JsonUtilTests {

	@Test
	public void createJsonByMap() {

		final String expectedResult = "{\"test1\":\"1\",\"test2\":\"2\",\"prim1\":1,\"add\":{\"add1\":\"3\",\"test\":true}}";
		final Map<String, Object> mainMap = new LinkedHashMap<>();
		mainMap.put("test1", "1");
		mainMap.put("test2", "2");
		mainMap.put("prim1", 1);

		final Map<String, Object> additionalMap = new LinkedHashMap<>();
		additionalMap.put("add1", "3");
		additionalMap.put("test", true);
		mainMap.put("add", additionalMap);

		String result = JsonUtil.getJsonByMap(mainMap);

		Assert.assertEquals(expectedResult, result);

	}

	@Test
	public void getMapByJson(){
		String imageProcessorName = "imageProcessor";
		String configurationProcessorName = "configurationProcessor";
		String json = "{\""+ imageProcessorName + "\":\"POST\",\"" + configurationProcessorName + "\":\"POST\"}";
		final Map<String, Object> resultMap = JsonUtil.getMapByJson(json);

		Assert.assertNotNull(resultMap);
		Assert.assertTrue(resultMap.containsKey(imageProcessorName));
		Assert.assertTrue(resultMap.containsKey(configurationProcessorName));
	}


	@Test
	public void jsonToMapTest(){
		String jsonNumbers = "{\"width\":600,\"height\":800,\"brightness\":80,\"timeout\":2}";
		String jsonStrings = "{\"width\":600,\"height\":800,\"brightness\":80,\"timeout\":2}";
		String jsonNumberBooleanMixed = "{\"width\":600,\"height\":true,\"brightness\":\"80\",\"timeout\":\"2\", \"text\":\"something\"}";

		final Map<String, Object> resultMap1 = JsonUtil.getMapByJson(jsonNumbers);
		final Map<String, Object> resultMap2 = JsonUtil.getMapByJson(jsonStrings);
		final Map<String, Object> resultMap3 = JsonUtil.getMapByJson(jsonNumberBooleanMixed);
		System.out.println("resultMap1: " + resultMap1);
		System.out.println("resultMap2: " + resultMap2);
		System.out.println("resultMap3: " + resultMap3);
	}
}
