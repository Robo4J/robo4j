package com.robo4j.socket.http.units;

import com.robo4j.socket.http.util.ExceptionMessageUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * configuring http client context
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ClientContext implements SocketContext<ClientPathConfig> {

	/**
	 * map of registered paths and related configuration
	 */
	private final Map<PathHttpMethod, ClientPathConfig> pathConfigs = new HashMap<>();

	/**
	 * context properties
	 */
	private final Map<String, Object> properties = new HashMap<>();

	public ClientContext() {
	}

	@Override
	public void addPaths(Map<PathHttpMethod, ClientPathConfig> paths) {
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
	public Collection<ClientPathConfig> getPathConfigs() {
		return pathConfigs.values();
	}

	@Override
	public ClientPathConfig getPathConfig(PathHttpMethod pathMethod) {
		return pathConfigs.getOrDefault(pathMethod, ClientPathConfig.EMPTY);
	}

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
		ClientContext context = (ClientContext) o;
		return Objects.equals(pathConfigs, context.pathConfigs) && Objects.equals(properties, context.properties);
	}

	@Override
	public int hashCode() {

		return Objects.hash(pathConfigs, properties);
	}

	@Override
	public String toString() {
		return "ClientContext{" + "pathConfigs=" + pathConfigs + '}';
	}
}
