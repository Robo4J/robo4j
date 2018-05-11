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

package com.robo4j.socket.http.units;

import com.robo4j.socket.http.HttpMethod;

import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class PathHttpMethod {

	private final String path;
	private final HttpMethod method;

	public PathHttpMethod(String path, HttpMethod method) {
		this.path = path;
		this.method = method;
	}

	public String getPath() {
		return path;
	}

	public HttpMethod getMethod() {
		return method;
	}

	@Override
	public String toString() {
		return "PathHttpMethod{" +
				"path='" + path + '\'' +
				", method=" + method +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PathHttpMethod that = (PathHttpMethod) o;
		return Objects.equals(path, that.path) && method == that.method;
	}

	@Override
	public int hashCode() {
		return Objects.hash(path, method);
	}
}
