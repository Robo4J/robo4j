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

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.robo4j.socket.http.util.ChannelBufferUtils.RESPONSE_SPRING_PATTERN;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Spring ResponseBody does contain additional formation
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class ChannelBufferTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelBufferTests.class);
    private static final int GROUP_COUNT = 3;
    private static final String JSON_MESSAGE = "{\"content\":\"No Params\",\"number\":11}";
    private static final String JSON_ARRAY_MESSAGE = "[{\"id\":\"stringConsumer\",\"state\":\"STARTED\"},{\"id\":\"httpServer\",\"state\":\"STARTED\"}]";

    @Test
    void springResponsePatternTest() {
        var message = "HTTP/1.1 200 \r\n" +
                "Content-Type: application/json;charset=UTF-8\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "Date: Mon, 05 Mar 2018 21:37:27 GMT\r\n" +
                "\r\n" +
                "23\r\n" + JSON_MESSAGE + "\r\n";
        var headerBody = message.split("\r\n\r\n");
        var matcher = RESPONSE_SPRING_PATTERN.matcher(headerBody[1]);
        if (matcher.find()) {
            printJson(matcher.group(2));
            assertEquals(GROUP_COUNT, matcher.groupCount());
            assertEquals(JSON_MESSAGE, matcher.group(2));
        }
    }


    @Test
    void jsonResponseStandardTest() {
        var message = "HTTP/1.1 200 \r\n" +
                "Content-Type: application/json;charset=UTF-8\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "Date: Mon, 05 Mar 2018 21:37:27 GMT\r\n" +
                "\r\n" + JSON_MESSAGE + "\r\n";

        var headerBody = message.split("\r\n\r\n");
        var matcher = RESPONSE_SPRING_PATTERN.matcher(headerBody[1]);
        if (matcher.find()) {
            printJson(matcher.group(2));
            assertEquals(JSON_MESSAGE, matcher.group(2));
        }
        assertEquals(GROUP_COUNT, matcher.groupCount());
    }

    @Test
    void jsonArrayResponseMessage() {
        var message = "HTTP/1.1 200 \r\n" +
                "Date: Mon, 05 Mar 2018 21:37:27 GMT\r\n" +
                "\r\n" + JSON_ARRAY_MESSAGE + "\r\n";

        var headerBody = message.split("\r\n\r\n");
        var matcher = RESPONSE_SPRING_PATTERN.matcher(headerBody[1]);
        if (matcher.find()) {
            printJson(matcher.group(2));
            assertEquals(JSON_ARRAY_MESSAGE, matcher.group(2));
        }
        assertEquals(GROUP_COUNT, matcher.groupCount());
    }

    private static void printJson(String resultJson) {
        LOGGER.debug("json:{}", resultJson);
    }
}
