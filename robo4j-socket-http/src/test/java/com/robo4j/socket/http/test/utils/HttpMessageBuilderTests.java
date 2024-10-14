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

import com.robo4j.socket.http.HttpHeaderFieldNames;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.ProtocolType;
import com.robo4j.socket.http.message.HttpRequestDenominator;
import com.robo4j.socket.http.util.HttpMessageBuilder;
import com.robo4j.socket.http.util.RoboHttpUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.robo4j.socket.http.provider.DefaultValuesProvider.BASIC_HEADER_MAP;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class HttpMessageBuilderTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpMessageBuilderTests.class);

    @Test
    void getRequestTest() {
        var getMessageBuilder = getDefaultMessageBuilderByMethod(HttpMethod.GET);

        var message = getMessageBuilder.build();

        LOGGER.info("toMessage: {}", message);
        assertNotNull(message);
        assertTrue(message.contains("GET / HTTP/1.1"));
        assertTrue(message.contains("host: localhost"));
    }

    @Test
    void postRequestTest() {
        String message = "magic";
        HttpMessageBuilder postMessageBuilder = getDefaultMessageBuilderByMethod(HttpMethod.POST);
        postMessageBuilder.addHeaderElement(HttpHeaderFieldNames.CONTENT_LENGTH, String.valueOf(message.length()));

        final String postRequest = postMessageBuilder.build(message);

        assertNotNull(postRequest);
        assertTrue(postRequest.contains("POST / HTTP/1.1"));
        assertTrue(postRequest.contains("host: localhost"));
        assertTrue(postRequest.contains("content-length: 5"));
        assertTrue(postRequest.contains(message));
    }


    private HttpMessageBuilder getDefaultMessageBuilderByMethod(HttpMethod method) {
        HttpRequestDenominator getDenominator = new HttpRequestDenominator(method, HttpVersion.HTTP_1_1);
        return HttpMessageBuilder.Build().setDenominator(getDenominator)
                .addHeaderElement(HttpHeaderFieldNames.HOST, RoboHttpUtils.createHost("localhost", ProtocolType.HTTP.getPort()))
                .addHeaderElements(BASIC_HEADER_MAP);
    }
}
