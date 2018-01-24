package com.robo4j.socket.http.units;

import java.util.Collection;

/**
 * interface for http context
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface HttpContext<T> {

	boolean isEmpty();

	boolean containsPath(String path);

	Collection<T> getPathConfigs();

	T getPathConfig(String path);

}
