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

package com.robo4j.http;

/**
 * Simple Message Wrapper to the incoming request GET/POST
 *
 * Simple HttpMessage is immutable
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpMessageWrapper<T> {

	private HttpMessage message;
	private T body;

	public HttpMessageWrapper(final HttpMessage message) {
		this.message = message;
		this.body = null;
	}

	public HttpMessageWrapper(final HttpMessage message, T body) {
		this.message = message;
		this.body = body;
	}

	public HttpMessage message() {
		return message;
	}

	public T body() {
		return body;
	}

	@Override
	public String toString() {
		return "HttpMessageWrapper{" + "message=" + message + ", body='" + body + '\'' + '}';
	}
}
