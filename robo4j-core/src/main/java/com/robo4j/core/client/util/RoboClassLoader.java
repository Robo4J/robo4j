/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 * 
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.core.client.util;

import java.io.InputStream;

/**
 *
 * Singleton instance of classLoader
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class RoboClassLoader {

	private static volatile RoboClassLoader INSTANCE;
	private volatile ClassLoader cl;

	private RoboClassLoader() {
		cl = Thread.currentThread().getContextClassLoader();
	}

	public static RoboClassLoader getInstance() {
		if (INSTANCE == null) {
			synchronized (RoboClassLoader.class) {
				if (INSTANCE == null) {
					INSTANCE = new RoboClassLoader();
				}
			}
		}
		return INSTANCE;
	}

	// Public Methods
	public ClassLoader getClassLoader() {
		return cl;
	}

	public InputStream getResource(final String name) {
		return cl.getResourceAsStream(name);
	}

}
