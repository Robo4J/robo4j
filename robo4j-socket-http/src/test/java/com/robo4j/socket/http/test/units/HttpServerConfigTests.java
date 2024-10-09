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
package com.robo4j.socket.http.test.units;

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.util.StringConstants;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * test for Http Server Unit configuration
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class HttpServerConfigTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerConfigTests.class);

    @Test
    void serverConfigurationNullTest() {
        Throwable exception = assertThrows(NullPointerException.class, () -> {
            HttpPathMethodDTO serverUnitPathDTO = HttpPathUtils.readServerPathDTO(null);
            assertNull(serverUnitPathDTO);
        });

        assertNotNull(exception.getMessage());
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
        var configurationJson = "{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\"}";
        var serverUnitPathDTO = HttpPathUtils.readServerPathDTO(configurationJson);

        LOGGER.info("serverUnitPathDTO: {}", serverUnitPathDTO);
        assertEquals("roboUnit1", serverUnitPathDTO.getRoboUnit());
        assertEquals(HttpMethod.GET, serverUnitPathDTO.getMethod());
        assertTrue(serverUnitPathDTO.getCallbacks().isEmpty());

    }

    @Test
    void serverConfigurationWithPropertiesParsingDTOTest() {
        var configurationJson = "{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\",\"callbacks\":[\"filter1\",\"filter2\"]}";
        var serverUnitPathDTO = HttpPathUtils.readServerPathDTO(configurationJson);

        assertEquals("roboUnit1", serverUnitPathDTO.getRoboUnit());
        assertEquals(HttpMethod.GET, serverUnitPathDTO.getMethod());
        assertArrayEquals(Arrays.asList("filter1", "filter2").toArray(), serverUnitPathDTO.getCallbacks().toArray());
    }

    @Test
    void serverConfigurationNullPathTest() {
        var paths = JsonUtil.readPathConfig(HttpPathMethodDTO.class, null);

        assertNotNull(paths);
        assertTrue(paths.isEmpty());

    }

    @Test
    void serverConfigurationEmptyPathTest() {
        var paths = JsonUtil.readPathConfig(HttpPathMethodDTO.class, StringConstants.EMPTY);

        assertNotNull(paths);
        assertTrue(paths.isEmpty());
    }

    @Test
    void serverConfigurationWithMultiplePathsWithoutPropertiesTest() {
        var configurationJson = "[{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\"},"
                + "{\"roboUnit\":\"roboUnit2\",\"method\":\"POST\"}]";

        var expectedPathList = Arrays.asList(new HttpPathMethodDTO("roboUnit1", HttpMethod.GET),
                new HttpPathMethodDTO("roboUnit2", HttpMethod.POST));

        var paths = JsonUtil.readPathConfig(HttpPathMethodDTO.class, configurationJson);

        assertEquals(expectedPathList.size(), paths.size());
        assertArrayEquals(expectedPathList.toArray(), paths.toArray());
    }

    @Test
    void serverConfigurationWithMultiplePathsWithPropertiesTest() {
        var configurationJson = "[{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\" , \"callbacks\":[\"filter1\",\"filter2\"]},"
                + "{\"roboUnit\":\"roboUnit2\",\"method\":\"POST\"}, {\"roboUnit\":\"roboUnit3\",\"method\":\"GET\",\"callbacks\":[]}]";

        var expectedPathList = Arrays.asList(
                new HttpPathMethodDTO("roboUnit1", HttpMethod.GET, Arrays.asList("filter1", "filter2")),
                new HttpPathMethodDTO("roboUnit2", HttpMethod.POST),
                new HttpPathMethodDTO("roboUnit3", HttpMethod.GET, Collections.emptyList()));

        var paths = JsonUtil.readPathConfig(HttpPathMethodDTO.class, configurationJson);

        LOGGER.info("paths: {}", paths);
        assertNotNull(paths);
        assertEquals(expectedPathList.size(), paths.size());
        assertArrayEquals(expectedPathList.toArray(), paths.toArray());
    }

}
