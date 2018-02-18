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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.socket.http.util;

import com.robo4j.socket.http.HttpHeaderFieldNames;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.ProtocolType;
import com.robo4j.socket.http.message.HttpRequestDenominator;
import org.junit.Assert;
import org.junit.Test;

import static com.robo4j.socket.http.provider.DefaultValuesProvider.BASIC_HEADER_MAP;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class HttpMessageBuilderTests {

	@Test
	public void getRequestTest() {
		HttpMessageBuilder getMessageBuilder = getDefaultMessageBuilderByMethod(HttpMethod.GET);

		final String getMessage = getMessageBuilder.build();
		Assert.assertNotNull(getMessage);
		Assert.assertTrue(getMessage.contains("GET / HTTP/1.1"));
		Assert.assertTrue(getMessage.contains("host: localhost"));
		System.out.println("toMessage: " + getMessage);
	}

	@Test
	public void postRequestTest() {
		String message = "magic";
		HttpMessageBuilder postMessageBuilder = getDefaultMessageBuilderByMethod(HttpMethod.POST);
		postMessageBuilder.addHeaderElement(HttpHeaderFieldNames.CONTENT_LENGTH, String.valueOf(message.length()));

		final String postRequest = postMessageBuilder.build(message);

		Assert.assertNotNull(postRequest);
		Assert.assertTrue(postRequest.contains("POST / HTTP/1.1"));
		Assert.assertTrue(postRequest.contains("host: localhost"));
		Assert.assertTrue(postRequest.contains("content-length: 5"));
		Assert.assertTrue(postRequest.contains(message));
	}

	@Test
	public void acceptedResponseTest() {

	}

	private HttpMessageBuilder getDefaultMessageBuilderByMethod(HttpMethod method) {
		HttpRequestDenominator getDenominator = new HttpRequestDenominator(method, HttpVersion.HTTP_1_1);
		return HttpMessageBuilder.Build().setDenominator(getDenominator)
				.addHeaderElement(HttpHeaderFieldNames.HOST, RoboHttpUtils.createHost("localhost", ProtocolType.HTTP.getPort()))
				.addHeaderElements(BASIC_HEADER_MAP);
	}
}
