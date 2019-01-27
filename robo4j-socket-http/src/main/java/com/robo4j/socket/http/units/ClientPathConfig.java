/*
 * Copyright (c) 2014-2019, Marcus Hirt, Miroslav Wengner
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
import com.robo4j.util.StringConstants;

import java.util.List;
import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ClientPathConfig {

	public static final ClientPathConfig EMPTY = new ClientPathConfig(StringConstants.EMPTY, null, null);

	private final String path;
	private final HttpMethod method;
	private final List<String> callbacks;

	public ClientPathConfig(String path, HttpMethod method, List<String> callbacks) {
		this.path = path;
		this.method = method;
		this.callbacks = callbacks;
	}

	public String getPath() {
		return path;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public List<String> getCallbacks() {
		return callbacks;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ClientPathConfig that = (ClientPathConfig) o;
		return Objects.equals(path, that.path) && method == that.method && Objects.equals(callbacks, that.callbacks);
	}

	@Override
	public int hashCode() {

		return Objects.hash(path, method, callbacks);
	}

	@Override
	public String toString() {
		return "ClientPathConfig{" + "path='" + path + '\'' + ", method=" + method + ", callbacks=" + callbacks + '}';
	}
}
