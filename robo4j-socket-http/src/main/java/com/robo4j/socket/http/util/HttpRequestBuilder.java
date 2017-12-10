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

import static com.robo4j.socket.http.provider.DefaultValuesProvider.BASIC_HEADER_MAP;
import static com.robo4j.socket.http.util.HttpConstant.STRING_EMPTY;
import static com.robo4j.socket.http.util.RoboHttpUtils.NEW_LINE_MAC;
import static com.robo4j.socket.http.util.RoboHttpUtils.NEW_LINE_UNIX;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class HttpRequestBuilder {

    private String host;
    private String path;
    private HttpMethod method;
    private HttpVersion version;
    private String message;


    private HttpRequestBuilder(){
    }

    public static HttpRequestBuilder Build(){
        return new HttpRequestBuilder();
    }

    public HttpRequestBuilder setMethod(HttpMethod method) {
        this.method = method;
        return this;
    }

    public HttpRequestBuilder setVersion(HttpVersion version) {
        this.version = version;
        return this;
    }

    public HttpRequestBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public HttpRequestBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    public HttpRequestBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public String build(){
        final HttpHeaderBuilder headerBuilder =  HttpHeaderBuilder.Build()
                .addFirstLine(path)
                .add(HttpHeaderFieldNames.HOST, host)
                .addAll(BASIC_HEADER_MAP);
        switch (method){
            case GET:
                return closeHeaderBuilder(headerBuilder);
            case POST:
                headerBuilder.add(HttpHeaderFieldNames.CONTENT_LENGTH, String.valueOf(message.length()));
                return closeHeaderBuilder(headerBuilder).concat(message);
        }
        return STRING_EMPTY;
    }


    private String closeHeaderBuilder(HttpHeaderBuilder headerBuilder){
        return headerBuilder.build(method, version)
                .concat(NEW_LINE_MAC).concat(NEW_LINE_UNIX);
    }

}
