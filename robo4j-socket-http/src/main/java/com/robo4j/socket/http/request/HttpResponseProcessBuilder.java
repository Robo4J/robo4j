/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.socket.http.request;

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.enums.StatusCode;

/**
 * {@link HttpResponseProcess}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class HttpResponseProcessBuilder {

	private String path;
	private String target;
	private HttpMethod method;
	private StatusCode code;
	private Object result;

	private HttpResponseProcessBuilder() {
	}

	static HttpResponseProcessBuilder Builder() {
		return new HttpResponseProcessBuilder();
	}

	public HttpResponseProcessBuilder setPath(String path) {
		this.path = path;
		return this;
	}

	public HttpResponseProcessBuilder setTarget(String target) {
		this.target = target;
		return this;
	}

	public HttpResponseProcessBuilder setMethod(HttpMethod method) {
		this.method = method;
		return this;
	}

	public HttpResponseProcessBuilder setCode(StatusCode code) {
		this.code = code;
		return this;
	}

	public HttpResponseProcessBuilder setResult(Object result) {
		this.result = result;
		return this;
	}

	public HttpResponseProcess build() {
		return new HttpResponseProcess(path, target, method, code, result);
	}
}
