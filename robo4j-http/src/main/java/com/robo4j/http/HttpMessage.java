/*
 * Copyright (C) 2016. Miroslav Wengner, Marcus Hirt
 * This HttpMessage.java is part of robo4j.
 *
 *     robo4j is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     robo4j is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.http;

import java.net.URI;
import java.util.Map;

/**
 * Http request message is immutable
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 * @since 09.03.2016
 */
public class HttpMessage implements Comparable<HttpMessage> {

	private final HttpMethod method;
	private final URI uri;
	private final HttpVersion version;
	private final Map<String, String> header;

	public HttpMessage(HttpMethod method, URI uri, HttpVersion version, Map<String, String> header) {
		this.method = method;
		this.uri = uri;
		this.version = version;
		this.header = header;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public URI getUri() {
		return uri;
	}

	public HttpVersion getVersion() {
		return version;
	}

	public Map<String, String> getHeader() {
		return header;
	}

	@Override
	public int compareTo(HttpMessage o) {
		return this.method.compareTo(o.method);
	}

	@Override
	public String toString() {
		return "HttpMessage{" + "method=" + method + ", uri=" + uri + ", version=" + version + ", header=" + header
				+ '}';
	}
}
