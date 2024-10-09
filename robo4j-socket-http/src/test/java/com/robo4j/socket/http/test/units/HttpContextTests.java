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

import com.robo4j.RoboBuilder;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.socket.http.test.units.config.StringConsumer;
import com.robo4j.socket.http.units.ClientContext;
import com.robo4j.socket.http.units.PathHttpMethod;
import com.robo4j.socket.http.units.ServerContext;
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.util.StringConstants;
import com.robo4j.util.Utf8Constant;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class HttpContextTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpContextTests.class);

    @Test
    void serverNotInitiatedContextTest() {
        ServerContext context = new ServerContext();

        printContext(context);
        assertNotNull(context);
        assertTrue(context.isEmpty());

    }

    @Test
    void serverDefaultContextTest() {
        var context = new ServerContext();
        HttpPathUtils.updateHttpServerContextPaths(null, context, Collections.emptyList());

        printContext(context);
        assertNotNull(context);
        assertFalse(context.isEmpty());
        assertTrue(context.containsPath(new PathHttpMethod(Utf8Constant.UTF8_SOLIDUS, HttpMethod.GET)));

    }

    @Test
    void clientDefaultContextTest() {
        var context = new ClientContext();

        printContext(context);
        assertNotNull(context);
        assertTrue(context.isEmpty());
    }

    @Test
    void clientSimpleContextTest() throws Exception {
        var builderProducer = new RoboBuilder();
        var contextIS = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("robo_client_context.xml");
        builderProducer.add(contextIS);
        var paths = Collections.singletonList(new HttpPathMethodDTO(StringConstants.EMPTY,
                HttpMethod.GET, Collections.singletonList(StringConsumer.NAME)));
        var context = new ClientContext();
        HttpPathUtils.updateHttpClientContextPaths(context, paths);

        var basicGet = new PathHttpMethod(Utf8Constant.UTF8_SOLIDUS, HttpMethod.GET);

        printContext(context);
        assertNotNull(context);
        assertNotNull(context.getPathConfig(basicGet));
        assertFalse(context.getPathConfig(basicGet).getCallbacks().isEmpty());
        assertEquals(HttpMethod.GET, context.getPathConfig(basicGet).getMethod());
        assertEquals(1, context.getPathConfig(basicGet).getCallbacks().size());
        assertEquals(StringConsumer.NAME, context.getPathConfig(basicGet).getCallbacks().get(0));
    }

    private static <T> void printContext(T context) {
        LOGGER.debug("context:{}", context);
    }
}
