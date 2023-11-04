/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.socket.http.util.ExceptionMessageUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration for http server unit {@link HttpServerUnit} Server context
 * contains available registered paths
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ServerContext implements SocketContext<ServerPathConfig> {

	/**
	 * map of registered paths and related configuration
	 */
	private final Map<PathHttpMethod, ServerPathConfig> pathConfigs = new HashMap<>();

	/**
	 * context properties
	 */
	private final Map<String, Object> properties = new HashMap<>();

	// TODO: review context creation public modifier due to the JPMS
	public ServerContext() {
	}

	@Override
	public void addPaths(Map<PathHttpMethod, ServerPathConfig> paths) {
		pathConfigs.putAll(paths);
	}

	@Override
	public boolean isEmpty() {
		return pathConfigs.isEmpty();
	}

	@Override
	public boolean containsPath(PathHttpMethod pathMethod) {
		return pathConfigs.containsKey(pathMethod);
	}

	@Override
	public Collection<ServerPathConfig> getPathConfigs() {
		return pathConfigs.values();
	}

	@Override
	public ServerPathConfig getPathConfig(PathHttpMethod pathMethod) {
		return pathConfigs.get(pathMethod);
	}

	/**
	 * null not allowed
	 * 
	 * @param key
	 *            string key
	 * @param val
	 *            value
	 */
	@Override
	public void putProperty(String key, Object val) {
		Objects.requireNonNull(key, ExceptionMessageUtils.mapMessage(key, val));
		Objects.requireNonNull(val, ExceptionMessageUtils.mapMessage(key, val));
		properties.put(key, val);
	}

	@Override
	public <E> E getProperty(Class<E> clazz, String key) {
		return properties.containsKey(key) ? clazz.cast(properties.get(key)) : null;
	}

	/**
	 *
	 * @param clazz
	 *            desired known class E
	 * @param key
	 *            property key
	 * @param <E>
	 *            property element instance
	 * @return property element
	 */
	@Override
	public <E> E getPropertySafe(Class<E> clazz, String key) {
		return clazz.cast(properties.get(key));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ServerContext that = (ServerContext) o;
		return Objects.equals(pathConfigs, that.pathConfigs) && Objects.equals(properties, that.properties);
	}

	@Override
	public int hashCode() {

		return Objects.hash(pathConfigs, properties);
	}

	@Override
	public String toString() {
		return "ServerContext{" + "pathConfigs=" + pathConfigs + '}';
	}
}
