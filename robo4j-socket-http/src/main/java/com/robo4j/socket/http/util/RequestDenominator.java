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

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;

import static com.robo4j.socket.http.util.HttpMessageUtil.SPACE;
import static com.robo4j.socket.http.util.JsonUtil.DEFAULT_PATH;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RequestDenominator implements HttpDenominator {

    private final StringBuilder sb = new StringBuilder();
    private final HttpMethod method;
    private final String path;
    private final HttpVersion version;

    public RequestDenominator(HttpMethod method, HttpVersion version) {
        this.method = method;
        this.path = DEFAULT_PATH;
        this.version = version;
    }

    public RequestDenominator(HttpMethod method, String path, HttpVersion version) {
        this.method = method;
        this.path = path;
        this.version = version;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String getVersion() {
        return version.getValue();
    }

    /**
     * Generate 1st line header
     * example : 'GET /path HTTP/1.1'
     *
     * @return 1st line
     */
    @Override
    public String generate() {
        assert method != null;
        assert path != null;
        assert version != null;
        return sb.append(method.getName())
                .append(SPACE)
                .append(path)
                .append(SPACE)
                .append(getVersion())
                .toString();
    }

    @Override
    public String toString() {
        return "RequestDenominator{" +
                "method=" + method +
                ", path='" + path + '\'' +
                ", version=" + version +
                '}';
    }
}
