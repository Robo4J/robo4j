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

import com.robo4j.socket.http.util.HttpHeaderBuilder;
import com.robo4j.socket.http.util.HttpMessageUtil;
import com.robo4j.socket.http.util.RoboHttpUtils;
import org.junit.Assert;
import org.junit.Test;

import static com.robo4j.socket.http.HttpHeaderFieldValues.CONNECTION_KEEP_ALIVE;

/**
 * Simple Http Header Oriented tests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpHeaderTests {

    private static final String CONST_USER_AGENT = "Robo4J-client";

    private static final String HEADER_HOST = "127.0.0.1";

    @Test
    public void createHeaderTest() {
        String header = HttpHeaderBuilder.Build()
                .addFirstLine("units/controller")
                .add(HttpHeaderFieldNames.HOST, HEADER_HOST).add(HttpHeaderFieldNames.CONNECTION, CONNECTION_KEEP_ALIVE)
                .add(HttpHeaderFieldNames.CACHE_CONTROL, HttpHeaderFieldValues.NO_CACHE).add(HttpHeaderFieldNames.USER_AGENT, CONST_USER_AGENT)
                .add(HttpHeaderFieldNames.ACCEPT, "*/*")
                .add(HttpHeaderFieldNames.ACCEPT_ENCODING, "gzip, deflate, sdch, br")
                .add(HttpHeaderFieldNames.ACCEPT_LANGUAGE, "en-US,en;q=0.8")
                .build(HttpMethod.GET, HttpVersion.HTTP_1_1);

        Assert.assertNotNull(header);
        Assert.assertEquals(header.split(HttpMessageUtil.NEXT_LINE).length, 8);
        Assert.assertEquals(header.split(HttpMessageUtil.NEXT_LINE)[2], craeteHeaderField(HttpHeaderFieldNames.CONNECTION, CONNECTION_KEEP_ALIVE));
        Assert.assertEquals(header.split(HttpMessageUtil.NEXT_LINE)[4], craeteHeaderField(HttpHeaderFieldNames.USER_AGENT, CONST_USER_AGENT));
    }

    @Test
    public void characterParser() {
        Assert.assertTrue("[".equals(HttpMessageUtil.getHttpSeparator(13)));
        Assert.assertTrue("]".equals(HttpMessageUtil.getHttpSeparator(14)));
    }

    @Test
    public void extractHeaderParameter() {
        String postRequest = RoboHttpUtils.createRequest(HttpMethod.POST, "127.0.0.1", "controller", "message");
        System.out.println("HEADER: " + postRequest);
    }

    private String craeteHeaderField(String key, String value){
        return key + ": " + value;
    }
}
