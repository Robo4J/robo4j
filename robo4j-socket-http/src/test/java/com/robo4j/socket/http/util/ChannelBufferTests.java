/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

import java.util.regex.Matcher;

import static com.robo4j.socket.http.util.ChannelBufferUtils.RESPONSE_SPRING_PATTERN;

/**
 * Spring ResponseBody does contain additional formation
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ChannelBufferTests {
    private static int GROUP_COUNT = 3;
    private static final String JSON_MESSAGE = "{\"content\":\"No Params\",\"number\":11}";
    private static final String JSON_ARRAY_MESSAGE = "[{\"id\":\"stringConsumer\",\"state\":\"STARTED\"},{\"id\":\"httpServer\",\"state\":\"STARTED\"}]";

    @Test
    public void springResponsePatternTest(){
        String message = "HTTP/1.1 200 \r\n" +
                "Content-Type: application/json;charset=UTF-8\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "Date: Mon, 05 Mar 2018 21:37:27 GMT\r\n" +
                "\r\n" +
                "23\r\n" + JSON_MESSAGE +"\r\n";
        String[] headerBody = message.split("\r\n\r\n");
        Matcher matcher = RESPONSE_SPRING_PATTERN.matcher(headerBody[1]);
        if(matcher.find()){
            System.out.println("json: " + matcher.group(2));
            Assert.assertTrue(matcher.groupCount() == GROUP_COUNT);
            Assert.assertTrue(matcher.group(2).equals(JSON_MESSAGE));
        }
    }


    @Test
    public void jsonResponseStandardTest(){
        String message = "HTTP/1.1 200 \r\n" +
                "Content-Type: application/json;charset=UTF-8\r\n" +
                "Transfer-Encoding: chunked\r\n" +
                "Date: Mon, 05 Mar 2018 21:37:27 GMT\r\n" +
                "\r\n" + JSON_MESSAGE +"\r\n";

        String[] headerBody = message.split("\r\n\r\n");
        Matcher matcher = RESPONSE_SPRING_PATTERN.matcher(headerBody[1]);
        if(matcher.find()){
            System.out.println("json: " + matcher.group(2));
            Assert.assertTrue(matcher.group(2).equals(JSON_MESSAGE));
        }
        Assert.assertTrue(matcher.groupCount() == GROUP_COUNT);
    }

    @Test
    public void jsonArrayResponseMessage(){
        String message = "HTTP/1.1 200 \r\n" +
                "Date: Mon, 05 Mar 2018 21:37:27 GMT\r\n" +
                "\r\n" + JSON_ARRAY_MESSAGE +"\r\n";

        String[] headerBody = message.split("\r\n\r\n");
        Matcher matcher = RESPONSE_SPRING_PATTERN.matcher(headerBody[1]);
        if(matcher.find()){
            System.out.println("json: " + matcher.group(2));
            Assert.assertTrue(matcher.group(2).equals(JSON_ARRAY_MESSAGE));
        }
        Assert.assertTrue(matcher.groupCount() == GROUP_COUNT);
    }

}
