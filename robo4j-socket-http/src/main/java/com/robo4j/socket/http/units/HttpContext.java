package com.robo4j.socket.http.units;

import java.util.Collection;
import java.util.Map;

/**
 * interface for http context
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface HttpContext<T> {

	void addPaths(Map<String, T> paths);

	boolean isEmpty();

	boolean containsPath(String path);

	Collection<T> getPathConfigs();

	T getPathConfig(String path);

	void putProperty(String key, Object val);

	<E> E getProperty(Class<E> clazz, String key);

	<E> E getPropertySafe(Class<E> clazz, String key);

}
