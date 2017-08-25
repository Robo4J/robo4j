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

import java.net.URI;
import java.util.Comparator;
import java.util.Map;

/**
 * Http request message is immutable
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
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

	public HttpMethod method() {
		return method;
	}

	public URI uri() {
		return uri;
	}

	public HttpVersion version() {
		return version;
	}

	public Map<String, String> header() {
		return header;
	}

	@Override
	public int compareTo(HttpMessage o) {
		//@formatter:off
		return Comparator.comparing((HttpMessage hm) -> hm.method)
				.thenComparing(hm -> hm.uri.getPath())
				.thenComparing(HttpMessage::version)
				.compare(this, o);
		//@formatter:on
	}

	@Override
	public String toString() {
		return "HttpMessage{" + "method=" + method + ", uri=" + uri + ", version=" + version + ", header=" + header
				+ '}';
	}
}
