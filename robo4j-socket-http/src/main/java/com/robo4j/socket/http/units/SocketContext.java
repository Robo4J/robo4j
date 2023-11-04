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

import java.util.Collection;
import java.util.Map;

/**
 * interface for channel context
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface SocketContext<T> {

	void addPaths(Map<PathHttpMethod, T> paths);

	boolean isEmpty();

	boolean containsPath(PathHttpMethod pathMethod);

	Collection<T> getPathConfigs();

	T getPathConfig(PathHttpMethod pathMethod);

	void putProperty(String key, Object val);

	<E> E getProperty(Class<E> clazz, String key);

	<E> E getPropertySafe(Class<E> clazz, String key);

}
