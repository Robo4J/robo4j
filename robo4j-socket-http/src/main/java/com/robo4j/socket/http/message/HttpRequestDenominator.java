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

package com.robo4j.socket.http.message;

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.util.Utf8Constant;

import static com.robo4j.util.Utf8Constant.UTF8_SOLIDUS;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class HttpRequestDenominator implements HttpDenominator {

	private final StringBuilder sb = new StringBuilder();
	private final HttpMethod method;
	private final String path;
	private final HttpVersion version;

	/**
	 * default request with default path
	 * 
	 * @param method
	 *            http method
	 * @param version
	 *            http version
	 */
	public HttpRequestDenominator(HttpMethod method, HttpVersion version) {
		this.method = method;
		this.path = UTF8_SOLIDUS;
		this.version = version;
	}

	/**
	 *
	 * @param method
	 *            http method
	 * @param path
	 *            server path
	 * @param version
	 *            http version
	 */
	public HttpRequestDenominator(HttpMethod method, String path, HttpVersion version) {
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
	 * Generate 1st line header example : 'GET /path HTTP/1.1'
	 *
	 * @return 1st line
	 */
	@Override
	public String generate() {
		assert method != null;
		assert path != null;
		assert version != null;
		return sb.append(method.getName()).append(Utf8Constant.UTF8_SPACE).append(path).append(Utf8Constant.UTF8_SPACE)
				.append(getVersion()).toString();
	}

	@Override
	public String toString() {
		return "HttpRequestDenominator{" + "method=" + method + ", path='" + path + '\'' + ", version=" + version + '}';
	}
}
