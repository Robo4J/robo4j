/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This ClientClassLoader.java  is part of robo4j.
 * module: robo4j-core
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.client.util;

import java.io.InputStream;

/**
 *
 * Singleton instance of classLoader
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 * @since 23.05.2016
 */
public final class ClientClassLoader {

	private static volatile ClientClassLoader INSTANCE;
	private volatile ClassLoader cl;

	private ClientClassLoader() {
		cl = Thread.currentThread().getContextClassLoader();
	}

	public static ClientClassLoader getInstance() {
		if (INSTANCE == null) {
			synchronized (ClientClassLoader.class) {
				if (INSTANCE == null) {
					INSTANCE = new ClientClassLoader();
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
