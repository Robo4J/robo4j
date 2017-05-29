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

package com.robo4j.core.httpunit.test;

import static junit.framework.TestCase.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;

import org.junit.Test;

import com.robo4j.core.client.util.RoboClassLoader;
import com.robo4j.core.httpunit.Constants;
import com.robo4j.core.util.IOUtil;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class JsonObjectTests {

	private static final String FILENAME_1 = "json_1.json";

	@Test
	public void createJsonObject() throws IOException {
		JsonObject model = Json.createObjectBuilder().add("uid", "robo4j")
				.add("configuration", Json.createArrayBuilder().add("httpClient").add("imageController")
						.add("scheduleController").add("httpServer"))
				.build();

		InputStream is = RoboClassLoader.getInstance().getResource(FILENAME_1);
		String fileJsonString = IOUtil.readString(is, Constants.DEFAULT_ENCODING);
		assertEquals(model.toString(), fileJsonString);

	}

	@Test
	public void createJsonObjectWithStreamFromMap() throws IOException {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("uid", "robo4j");
		List<String> configArray = new ArrayList<>();
		configArray.add("httpClient");
		configArray.add("imageController");
		configArray.add("scheduleController");
		configArray.add("httpServer");
		map.put("configuration", configArray);
		JsonObject model = Json.createObjectBuilder(map).build();
		StringWriter stringWriter = new StringWriter();
		JsonWriter jsonWriter = Json.createWriter(stringWriter);
		jsonWriter.writeObject(model);
		jsonWriter.close();

		String jsonData = stringWriter.toString();

		InputStream is = RoboClassLoader.getInstance().getResource(FILENAME_1);
		String fileJsonString = IOUtil.readString(is, Constants.DEFAULT_ENCODING);
		assertEquals(jsonData, fileJsonString);
	}

}
