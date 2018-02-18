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

package com.robo4j.socket.http;

import com.robo4j.socket.http.enums.StatusCode;
import com.robo4j.socket.http.provider.DefaultValuesProvider;
import com.robo4j.socket.http.message.HttpDenominator;
import com.robo4j.socket.http.util.HttpHeaderBuilder;
import com.robo4j.socket.http.util.HttpMessageBuilder;
import com.robo4j.socket.http.util.HttpMessageUtils;
import com.robo4j.socket.http.message.HttpRequestDenominator;
import com.robo4j.socket.http.util.RoboHttpUtils;
import org.junit.Assert;
import org.junit.Test;

import static com.robo4j.socket.http.HttpHeaderFieldValues.CONNECTION_KEEP_ALIVE;
import static com.robo4j.socket.http.provider.DefaultValuesProvider.ROBO4J_CLIENT;
import static com.robo4j.socket.http.util.HttpConstant.HTTP_NEW_LINE;

/**
 * Simple Http Header Oriented tests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpHeaderTests {

	@Test
	public void createHeaderTest() {
		String header = HttpHeaderBuilder.Build().addFirstLine("units/controller")
				.addAll(DefaultValuesProvider.BASIC_HEADER_MAP).build(HttpMethod.GET, HttpVersion.HTTP_1_1);

		Assert.assertNotNull(header);
		Assert.assertEquals(header.split(HTTP_NEW_LINE).length, 8);
		Assert.assertEquals(header.split(HTTP_NEW_LINE)[2],
				createHeaderField(HttpHeaderFieldNames.USER_AGENT, ROBO4J_CLIENT));
		Assert.assertEquals(header.split(HTTP_NEW_LINE)[3],
				createHeaderField(HttpHeaderFieldNames.CONNECTION, CONNECTION_KEEP_ALIVE));
	}

	@Test
	public void characterParser() {
		Assert.assertTrue("[".equals(HttpMessageUtils.getHttpSeparator(13)));
		Assert.assertTrue("]".equals(HttpMessageUtils.getHttpSeparator(14)));
	}

	@Test
	public void test() {
		String uid = "1234";
		String expectedResult = "HTTP/1.1 200 OK" + HTTP_NEW_LINE + "uid: " + uid + HTTP_NEW_LINE;
		//@formatter:off
        String getHeader =  HttpHeaderBuilder.Build()
                .addFirstLine(HttpVersion.HTTP_1_1.getValue())
                .addFirstLine(StatusCode.OK.getCode())
                .addFirstLine(StatusCode.OK.getReasonPhrase())
                .add(HttpHeaderFieldNames.ROBO_UNIT_UID, uid)
                .build();
        //@formatter:on
		Assert.assertTrue(getHeader.equals(expectedResult));
	}

	@Test
	public void extractHeaderParameter() {
		String message = "message";
		HttpDenominator denominator = new HttpRequestDenominator(HttpMethod.POST, HttpVersion.HTTP_1_1);
		String postRequest = HttpMessageBuilder.Build().setDenominator(denominator)
				.addHeaderElement(HttpHeaderFieldNames.HOST, RoboHttpUtils.createHost("127.0.0.1", ProtocolType.HTTP.getPort()))
				.addHeaderElement(HttpHeaderFieldNames.CONTENT_LENGTH, String.valueOf(message.length()))
				.build(message);
		System.out.println("HEADER: " + postRequest);
	}

	private String createHeaderField(String key, String value) {
		return key + ": " + value;
	}
}
