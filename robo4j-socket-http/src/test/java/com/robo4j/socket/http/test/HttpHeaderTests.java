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
package com.robo4j.socket.http.test;

import com.robo4j.socket.http.HttpHeaderFieldNames;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.ProtocolType;
import com.robo4j.socket.http.enums.StatusCode;
import com.robo4j.socket.http.message.HttpDenominator;
import com.robo4j.socket.http.message.HttpRequestDenominator;
import com.robo4j.socket.http.provider.DefaultValuesProvider;
import com.robo4j.socket.http.util.HttpHeaderBuilder;
import com.robo4j.socket.http.util.HttpMessageBuilder;
import com.robo4j.socket.http.util.HttpMessageUtils;
import com.robo4j.socket.http.util.RoboHttpUtils;
import org.junit.jupiter.api.Test;

import static com.robo4j.socket.http.HttpHeaderFieldValues.CONNECTION_KEEP_ALIVE;
import static com.robo4j.socket.http.provider.DefaultValuesProvider.ROBO4J_CLIENT;
import static com.robo4j.socket.http.util.HttpConstant.HTTP_NEW_LINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Simple Http Header Oriented tests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
class HttpHeaderTests {

	@Test
	void createHeaderTest() {
		String header = HttpHeaderBuilder.Build().addFirstLine("units/controller")
				.addAll(DefaultValuesProvider.BASIC_HEADER_MAP).build(HttpMethod.GET, HttpVersion.HTTP_1_1);

		assertNotNull(header);
		assertEquals(8, header.split(HTTP_NEW_LINE).length);
		assertEquals(header.split(HTTP_NEW_LINE)[2],
				createHeaderField(HttpHeaderFieldNames.USER_AGENT, ROBO4J_CLIENT));
		assertEquals(header.split(HTTP_NEW_LINE)[3],
				createHeaderField(HttpHeaderFieldNames.CONNECTION, CONNECTION_KEEP_ALIVE));
	}

	@Test
	void characterParser() {
		assertEquals("[", HttpMessageUtils.getHttpSeparator(13));
		assertEquals("]",HttpMessageUtils.getHttpSeparator(14));
	}

	@Test
	void test() {
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
		assertEquals(expectedResult, getHeader);
	}

	@Test
	void extractHeaderParameter() {
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
