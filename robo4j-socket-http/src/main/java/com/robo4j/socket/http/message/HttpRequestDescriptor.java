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

package com.robo4j.socket.http.message;

import com.robo4j.socket.http.HttpMethod;

import java.util.Map;

/**
 * Inbound Http message used by Server units.
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class HttpRequestDescriptor extends AbstractHttpMessageDescriptor {

	final private HttpMethod method;
	final private String path;

	public HttpRequestDescriptor(Map<String, String> header, HttpMethod method, String version, String path) {
		super(header, version);
		this.method = method;
		this.path = path;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	@Override
	public String toString() {
		return "HttpRequestDescriptor{" + ", method=" + method + ", path='" + path + '\'' + super.toString()
				+ '}';
	}
}
