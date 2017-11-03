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
import com.robo4j.socket.http.util.ByteBufferUtils;
import com.robo4j.socket.http.util.RoboHttpUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

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
			"Cache-Control: no-cache\n" +
			"Origin: chrome-extension://fhbjgbiflinjbdggehcddcbncdddomop\r\n" +
			"User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36\r\n" +
			"Content-Type: text/plain;charset=UTF-8\r\n" +
			"Accept: */*\r\n" +
			"Accept-Encoding: gzip, deflate, br\r\n" +
			"Accept-Language: en-US,en;q=0.8\r\n" +
			"\r\n" +
			"{ \n" +
			"  \"value\" : \"move\"\n" +
			"}";
	private static final ByteBuffer TEST_BYTE_BUFFER = ByteBuffer.wrap(TEST_STRING.getBytes());

	@Test
	public void testPostmanMessage(){
		ByteBuffer incomingBuffer = ByteBuffer.wrap(TEST_POSTMAN_STRING.getBytes());
		BufferWrapper bufferWrapper = new BufferWrapper(incomingBuffer, TEST_POSTMAN_STRING.length());
		HttpByteWrapper wrapper = ByteBufferUtils.getHttpByteWrapperByByteBuffer(bufferWrapper);

		String resultValue = new String(wrapper.getHeader().array());
		String resultBody = new String(wrapper.getBody().array());

		System.out.println("VALUE: " + resultValue);
		System.out.println("BODY: " + resultBody);

	}

	@Test
	public void testSimpleByteBufferFromRequest() {

		String bodyMessage = "this is test message";
		String client = "http://0.0.0.0:8080";
		String clientUri = "/test";

		String postHeader = RoboHttpUtils.createHeader(HttpMethod.POST, client, clientUri, bodyMessage);
		String correctedPostHeader = postHeader.trim();
		String postMessage = RoboHttpUtils.createRequest(HttpMethod.POST, client, clientUri, bodyMessage);

		ByteBuffer incomingBuffer = ByteBuffer.wrap(postMessage.getBytes());
		BufferWrapper bufferWrapper = new BufferWrapper(incomingBuffer, postMessage.length());
		HttpByteWrapper wrapper = ByteBufferUtils.getHttpByteWrapperByByteBuffer(bufferWrapper);

		String resultHeader = new String(wrapper.getHeader().array());
		String resultBody = new String(wrapper.getBody().array());

		Assert.assertNotNull(postMessage);
		Assert.assertTrue(correctedPostHeader.length() == resultHeader.length());
		Assert.assertTrue(correctedPostHeader.equals(new String(wrapper.getHeader().array())));
		Assert.assertTrue(bodyMessage.equals(resultBody));
	}

	@Test
	public void testMovingWindow() {
		String correctString = "\n\n";
		Assert.assertTrue(ByteBufferUtils.isBWindow(ByteBufferUtils.END_WINDOW,
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
			if (b == ByteBufferUtils.CHAR_RETURN) {
			} else {
				tmpBytes[bPosition] = b;
				bPosition++;
			}
			position++;
		}

		byte[] resBytes = ByteBufferUtils.validArray(tmpBytes, bPosition);
		ByteBuffer resultBuffer = ByteBuffer.wrap(resBytes);
		Assert.assertTrue(TEST_BYTE_BUFFER.capacity() == resultBuffer.capacity() + ByteBufferUtils.END_WINDOW.length);
	}
}
