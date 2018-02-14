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

package com.robo4j.socket.http.request;

import com.robo4j.socket.http.HttpHeaderFieldNames;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.ProtocolType;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.message.HttpDenominator;
import com.robo4j.socket.http.message.HttpRequestDenominator;
import com.robo4j.socket.http.util.ChannelBufferUtils;
import com.robo4j.socket.http.util.HttpMessageBuilder;
import com.robo4j.socket.http.util.RoboHttpUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.robo4j.socket.http.HttpHeaderFieldValues.CONNECTION_KEEP_ALIVE;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ByteBufferTests {

    private static final String TEST_STRING = "Accept-Language: en-US,en;q=0.8\r\n\r\n{";
    private static final String TEST_POSTMAN_MESSAGE = "\r\n" +
            "{ \n" +
            "  \"value\" : \"move\"\n" +
            "}";
    private static final String TEST_POSTMAN_STRING = "POST /controller HTTP/1.1\r\n" +
            "Host: localhost:8042\r\n" +
            "Connection: " + CONNECTION_KEEP_ALIVE + "\r\n" +
            "Content-Length: 23\r\n" +
            "Postman-Token: 60b492c5-e7a9-6037-3021-42f8885542a9\r\n" +
            "Cache-Control: no-cache\r\n" +
            "Origin: chrome-extension://fhbjgbiflinjbdggehcddcbncdddomop\r\n" +
            "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36\r\n" +
            "Content-Type: text/plain;charset=UTF-8\r\n" +
            "Accept: */*\r\n" +
            "Accept-Encoding: gzip, deflate, br\r\n" +
            "Accept-Language: en-US,en;q=0.8\r\n" +
            TEST_POSTMAN_MESSAGE;
    private static final ByteBuffer TEST_BYTE_BUFFER = ByteBuffer.wrap(TEST_STRING.getBytes());

    @Test
    public void testPostmanMessage() {
        HttpDecoratedRequest decoratedRequest = ChannelBufferUtils.extractDecoratedRequestByStringMessage(TEST_POSTMAN_STRING);
        decoratedRequest.addMessage(TEST_POSTMAN_MESSAGE);

        Assert.assertNotNull(decoratedRequest.getHeader());
        Assert.assertTrue(!decoratedRequest.getHeader().isEmpty());
        Assert.assertNotNull(decoratedRequest.getMessage());
        System.out.println("HEADER: " + decoratedRequest.getHeader());
        System.out.println("BODY: " + decoratedRequest.getMessage());

    }

    @Test
    public void byteBufferFromRequestTest() {

        String bodyMessage = "this is test message";
        String host = "0.0.0.0";
        Integer port = 8080;
        String clientPath = "/test";

        HttpDenominator denominator = new HttpRequestDenominator(HttpMethod.POST, clientPath, HttpVersion.HTTP_1_1);
        String postMessage = HttpMessageBuilder.Build()
                .setDenominator(denominator)
                .addHeaderElement(HttpHeaderFieldNames.CONTENT_LENGTH, String.valueOf(bodyMessage.length()))
                .addHeaderElement(HttpHeaderFieldNames.HOST, RoboHttpUtils.createHost(host, ProtocolType.HTTP.getPort()))
                .build(bodyMessage);

        HttpDecoratedRequest decoratedRequest = ChannelBufferUtils.extractDecoratedRequestByStringMessage(postMessage);

        Assert.assertNotNull(postMessage);
        Assert.assertTrue(postMessage.length() == decoratedRequest.getLength());
        Assert.assertTrue(clientPath.equals(decoratedRequest.getPath()));
        Assert.assertTrue(bodyMessage.equals(decoratedRequest.getMessage()));
    }

    @Test
    public void testMovingWindow() {
        String correctString = "\n\n";
        Assert.assertTrue(ChannelBufferUtils.isBWindow(ChannelBufferUtils.END_WINDOW,
                ByteBuffer.wrap(correctString.getBytes()).array()));
    }

    @Test
    public void testReturnCharRemoval() {

        int position = 0;
        int bPosition = 0;
        int size = TEST_BYTE_BUFFER.capacity();
        byte[] tmpBytes = new byte[1024];

        while (position < size) {
            byte b = TEST_BYTE_BUFFER.get(position);
            if (b == ChannelBufferUtils.CHAR_RETURN) {
            } else {
                tmpBytes[bPosition] = b;
                bPosition++;
            }
            position++;
        }

        byte[] resBytes = ChannelBufferUtils.validArray(tmpBytes, bPosition);
        ByteBuffer resultBuffer = ByteBuffer.wrap(resBytes);
        Assert.assertTrue(TEST_BYTE_BUFFER.capacity() == resultBuffer.capacity() + ChannelBufferUtils.END_WINDOW.length);
    }

    @Test
    public void test() {
        Pattern pattern = Pattern.compile("^(.*)[\\r\\n{2}]|[\\n\\n](.*)$");

        String one = "some\r\n\r\nother";
        String two = "some\n\nother";

        Matcher matcher1 = pattern.matcher(one);
        Matcher matcher2 = pattern.matcher(two);

//		Assert.assertTrue(matcher1.find());
//		Assert.assertTrue(matcher2.find());
//		Assert.assertTrue(matcher2.groupCount() == matcher1.groupCount());
//		Assert.assertTrue(matcher2.groupCount() == matcher1.groupCount());
        System.out.println("ONE: " + matcher1.find() + " group: " + matcher1.groupCount() + " 1: " + matcher1.group(1) + " 2: " + matcher1.group(0));
        System.out.println("TWO: " + matcher2.find() + " group: " + matcher2.groupCount());

    }
}
