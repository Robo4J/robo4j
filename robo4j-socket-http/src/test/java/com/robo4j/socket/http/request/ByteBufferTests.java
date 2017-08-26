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

	@Test
	public void testSimpleByteBufferFromRequest() {

		String bodyMessage = "this is test message";
		String client = "http://0.0.0.0:8080";
		String clientUri = "/test";

		String postHeader = RoboHttpUtils.createHeader(HttpMethod.POST, client, clientUri, bodyMessage);
		String postHeaderAdjusted = postHeader.substring(0, postHeader.length()-1);
		String postMessage = RoboHttpUtils.createRequest(HttpMethod.POST, client, clientUri, bodyMessage);

		ByteBuffer incomingBuffer = ByteBuffer.wrap(postMessage.getBytes());
		HttpByteWrapper wrapper = ByteBufferUtils.getHttpByteWrapperByByteBuffer(incomingBuffer);

        Assert.assertNotNull(postMessage);
		Assert.assertTrue(postHeaderAdjusted.equals(new String(wrapper.getHeader().array())));
		Assert.assertTrue(bodyMessage.equals(new String(wrapper.getBody().array())));
	}

}
