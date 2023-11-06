/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.test.units;

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.util.StringConstants;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * test for Http Server Unit configuration
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class HttpServerConfigTests {

	@Test
	void serverConfigurationNullTest() {

		assertThrows(NullPointerException.class, () -> {
			HttpPathMethodDTO serverUnitPathDTO = HttpPathUtils.readServerPathDTO(null);
			assertNull(serverUnitPathDTO);
		});

	}

	@Test
	void serverConfigurationEmptyTest() {
		Throwable exception = assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
			HttpPathUtils.readServerPathDTO(StringConstants.EMPTY);
		});

		assertEquals("Index 0 out of bounds for length 0", exception.getMessage());
	}

	@Test
	void serverConfigurationWithoutPropertiesDTOTest() {

		String configurationJson = "{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\"}";
		HttpPathMethodDTO serverUnitPathDTO = HttpPathUtils.readServerPathDTO(configurationJson);

		System.out.println("serverUnitPathDTO: " + serverUnitPathDTO);
		assertEquals("roboUnit1", serverUnitPathDTO.getRoboUnit());
		assertEquals(HttpMethod.GET, serverUnitPathDTO.getMethod());
		assertTrue(serverUnitPathDTO.getCallbacks().isEmpty());

	}

	@Test
	void serverConfigurationWithPropertiesParsingDTOTest() {

		String configurationJson = "{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\",\"callbacks\":[\"filter1\",\"filter2\"]}";
		HttpPathMethodDTO serverUnitPathDTO = HttpPathUtils.readServerPathDTO(configurationJson);

		assertEquals("roboUnit1", serverUnitPathDTO.getRoboUnit());
		assertEquals(HttpMethod.GET, serverUnitPathDTO.getMethod());
		assertArrayEquals(Arrays.asList("filter1", "filter2").toArray(), serverUnitPathDTO.getCallbacks().toArray());

		System.out.println("serverUnitPathDTO: " + serverUnitPathDTO);

	}

	@Test
	void serverConfigurationNullPathTest() {
		List<HttpPathMethodDTO> paths = JsonUtil.readPathConfig(HttpPathMethodDTO.class, null);
		assertNotNull(paths);
		assertTrue(paths.isEmpty());

	}

	@Test
	void serverConfigurationEmptyPathTest() {
		List<HttpPathMethodDTO> paths = JsonUtil.readPathConfig(HttpPathMethodDTO.class, StringConstants.EMPTY);
		assertNotNull(paths);
		assertTrue(paths.isEmpty());

	}

	@Test
	void serverConfigurationWithMultiplePathsWithoutPropertiesTest() {
		String configurationJson = "[{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\"},"
				+ "{\"roboUnit\":\"roboUnit2\",\"method\":\"POST\"}]";

		List<HttpPathMethodDTO> expectedPathList = Arrays.asList(new HttpPathMethodDTO("roboUnit1", HttpMethod.GET),
				new HttpPathMethodDTO("roboUnit2", HttpMethod.POST));

		List<HttpPathMethodDTO> paths = JsonUtil.readPathConfig(HttpPathMethodDTO.class, configurationJson);

		assertEquals(expectedPathList.size(), paths.size());
		assertArrayEquals(expectedPathList.toArray(), paths.toArray());
	}

	@Test
	void serverConfigurationWithMultiplePathsWithPropertiesTest() {
		String configurationJson = "[{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\" , \"callbacks\":[\"filter1\",\"filter2\"]},"
				+ "{\"roboUnit\":\"roboUnit2\",\"method\":\"POST\"}, {\"roboUnit\":\"roboUnit3\",\"method\":\"GET\",\"callbacks\":[]}]";

		List<HttpPathMethodDTO> expectedPathList = Arrays.asList(
				new HttpPathMethodDTO("roboUnit1", HttpMethod.GET, Arrays.asList("filter1", "filter2")),
				new HttpPathMethodDTO("roboUnit2", HttpMethod.POST),
				new HttpPathMethodDTO("roboUnit3", HttpMethod.GET, Collections.emptyList()));

		List<HttpPathMethodDTO> paths = JsonUtil.readPathConfig(HttpPathMethodDTO.class, configurationJson);
		System.out.println("paths: " + paths);

		assertNotNull(paths);
		assertEquals(expectedPathList.size(), paths.size());
		assertArrayEquals(expectedPathList.toArray(), paths.toArray());
	}

}
