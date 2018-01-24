package com.robo4j.socket.http.units;

import com.robo4j.socket.http.HttpMethod;

import java.util.List;
import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ClientPathConfig {

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
