/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.spring;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * RoboSpringRegisterCacheImpl registers components available in Spring Context
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class RoboSpringRegisterCacheImpl implements RoboSpringRegister {

	private final Map<String, Object> registerCache = new WeakHashMap<>();

	RoboSpringRegisterCacheImpl() {
	}

	/**
	 * Register Spring component
	 * 
	 * @param name
	 *            component name
	 * @param instance
	 *            component instance
	 */
	@Override
	public void register(String name, Object instance) {
		registerCache.putIfAbsent(name, instance);
	}

	@Override
	public Object getComponent(String name) {
		return registerCache.get(name);
	}

	@Override
	public boolean containsComponent(String name) {
		return registerCache.containsKey(name);
	}
}
