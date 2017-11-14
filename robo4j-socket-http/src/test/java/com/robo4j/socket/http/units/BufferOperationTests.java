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

package com.robo4j.socket.http.units;

import org.junit.Test;

import java.util.Arrays;

import static com.robo4j.socket.http.HttpHeaderFieldValues.CONNECTION_KEEP_ALIVE;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class BufferOperationTests {

    private static final String REQUEST_MESSAGE = "POST units/controller HTTP/1.1\n" +
            "host: 0.0.0.0\n" +
            "connection:" + CONNECTION_KEEP_ALIVE + "\n" +
            "cache-control: no-cache\n" +
            "user-agent: Robo4J-HttpClient\n" +
            "accept: */*\n" +
            "accept-encoding: gzip, deflate, sdch, br\n" +
            "accept-language: en-US,en;q=0.8\n" +
            "content-type: text/html; charset=utf-8\n" +
            "content-length: 16\n" +
            "\n" +
            "{\"value\":\"stop\"}";

    @Test
    public void testExtractedStringMessage() {
        String[] headerAndBody = REQUEST_MESSAGE.split("\n\n");
        String[] header = headerAndBody[0].split("[\r\n]+");

        System.out.println("Header first: " + header[0]);
        System.out.println("Header full: " + Arrays.asList(header));
        System.out.println("Body: " + headerAndBody[1]);
    }


}
