package com.robo4j.socket.http.units;

import com.robo4j.RoboReference;
import com.robo4j.socket.http.HttpMethod;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ServerPathConfig {

	private final String path;
	private final RoboReference<Object> roboUnit;
	private final HttpMethod method;
	private final List<String> filters;

	public ServerPathConfig(String path, RoboReference<Object> roboUnit, HttpMethod method) {
		this.path = path;
		this.roboUnit = roboUnit;
		this.method = method;
		this.filters = null;
	}

	public ServerPathConfig(String path, RoboReference<Object> roboUnit, HttpMethod method, List<String> filters) {
		this.path = path;
		this.roboUnit = roboUnit;
		this.method = method;
		this.filters = filters;
	}

	public String getPath() {
		return path;
	}

	public RoboReference<Object> getRoboUnit() {
		return roboUnit;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public List<String> getFilters() {
		return Collections.unmodifiableList(filters);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ServerPathConfig that = (ServerPathConfig) o;
		return Objects.equals(path, that.path) && Objects.equals(roboUnit, that.roboUnit) && method == that.method
				&& Objects.equals(filters, that.filters);
	}

	@Override
	public int hashCode() {

		return Objects.hash(path, roboUnit, method, filters);
	}

	@Override
	public String toString() {
		return "ServerPathConfig{" + "path='" + path + '\'' + ", roboUnit=" + roboUnit + ", method=" + method
				+ ", filters=" + filters + '}';
	}
}
