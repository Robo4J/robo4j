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

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.socket.http.util.HttpHeaderBuilder;
import com.robo4j.socket.http.util.HttpMessageUtil;

/**
 *
 * Simple Http Header Oriented tests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpHeaderTests {

	private static final String CONST_CACHE_CONTROL = "keep-alive";
	private static final String CONST_USER_AGENT = "Robo4J-client";

	@Test
	public void simpleHttpHeader() {
		// formatter:off
		final String header = HttpHeaderBuilder.Build().add(HttpHeaderFieldNames.HOST, "127.0.0.1")
				.add(HttpHeaderFieldNames.CONNECTION, CONST_CACHE_CONTROL).add(HttpHeaderFieldNames.CACHE_CONTROL, "no-cache")
				.add(HttpHeaderFieldNames.USER_AGENT, CONST_USER_AGENT).add(HttpHeaderFieldNames.ACCEPT, "*/*")
				.add(HttpHeaderFieldNames.ACCEPT_ENCODING, "gzip, deflate, sdch, br")
				.add(HttpHeaderFieldNames.ACCEPT_LANGUAGE, "en-US,en;q=0.8").build();
		// formatter:on
		Assert.assertNotNull(header);
		Assert.assertEquals(header.split(HttpMessageUtil.NEXT_LINE).length, 7);
		Assert.assertEquals(header.split(HttpMessageUtil.NEXT_LINE)[1],
				HttpHeaderBuilder.Build().add(HttpHeaderFieldNames.CONNECTION, CONST_CACHE_CONTROL).build().trim());
		Assert.assertEquals(header.split(HttpMessageUtil.NEXT_LINE)[3],
				HttpHeaderBuilder.Build().add(HttpHeaderFieldNames.USER_AGENT, CONST_USER_AGENT).build().trim());
	}

}
