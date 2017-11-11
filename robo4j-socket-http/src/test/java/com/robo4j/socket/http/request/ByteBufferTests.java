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

import com.robo4j.socket.http.HttpByteWrapper;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.units.BufferWrapper;
import com.robo4j.socket.http.util.ChannelBufferUtils;
import com.robo4j.socket.http.util.RoboHttpUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ByteBufferTests {

	private static final String TEST_STRING = "Accept-Language: en-US,en;q=0.8\r\n\r\n{";
	private static final String TEST_POSTMAN_STRING = "POST /controller HTTP/1.1\r\n" +
			"Host: localhost:8042\r\n" +
			"Connection: keep-alive\r\n" +
			"Content-Length: 23\r\n" +
			"Postman-Token: 60b492c5-e7a9-6037-3021-42f8885542a9\r\n" +
			"Cache-Control: no-cache\r\n" +
			"Origin: chrome-extension://fhbjgbiflinjbdggehcddcbncdddomop\r\n" +
			"User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36\r\n" +
			"Content-Type: text/plain;charset=UTF-8\r\n" +
			"Accept: */*\r\n" +
			"Accept-Encoding: gzip, deflate, br\r\n" +
			"Accept-Language: en-US,en;q=0.8\r\n" +
			"\n" +
			"{ \n" +
			"  \"value\" : \"move\"\n" +
			"}";
	private static final ByteBuffer TEST_BYTE_BUFFER = ByteBuffer.wrap(TEST_STRING.getBytes());

	@Test
	public void testPostmanMessage(){
		BufferWrapper bufferWrapper = new BufferWrapper(TEST_POSTMAN_STRING.length(), TEST_POSTMAN_STRING);
		HttpByteWrapper wrapper = ChannelBufferUtils.getHttpByteWrapperByByteBufferString(bufferWrapper);

		String[] headerResult = wrapper.getHeader();
		String resultBody = wrapper.getBody();

		Assert.assertNotNull(headerResult);
		Assert.assertNotNull(resultBody);
		System.out.println("VALUE: " + Arrays.asList(headerResult));
		System.out.println("BODY: " + resultBody);

	}

	@Test
	public void testSimpleByteBufferFromRequest() {

		String bodyMessage = "this is test message";
		String client = "http://0.0.0.0:8080";
		String clientUri = "/test";

		String postHeader = RoboHttpUtils.createHeader(HttpMethod.POST, client, clientUri, bodyMessage.length());
		String correctedPostHeader = postHeader.trim();
		String postMessage = RoboHttpUtils.createRequest(HttpMethod.POST, client, clientUri, bodyMessage);

		BufferWrapper bufferWrapper = new BufferWrapper(postMessage.length(), postMessage);
		HttpByteWrapper wrapper = ChannelBufferUtils.getHttpByteWrapperByByteBufferString(bufferWrapper);

		String[] resultHeader = wrapper.getHeader();
		String resultBody = wrapper.getBody();

		String httpHeaderString = Arrays.stream(resultHeader).collect(Collectors.joining("\r\n"));
		Assert.assertNotNull(postMessage);
		Assert.assertTrue(correctedPostHeader.length() == httpHeaderString.length());
		Assert.assertTrue(correctedPostHeader.equals(httpHeaderString));
		Assert.assertTrue(bodyMessage.equals(resultBody));
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
}
