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
package com.robo4j.socket.http;

import java.util.HashMap;
import java.util.Map;

/**
 * Holder for properties,
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class PropertiesProvider {

	private final Map<String, Object> map = new HashMap<>();

	public PropertiesProvider() {
	}

	public void put(String key, Object val) {
		map.put(key, val);
	}

	public PropertiesProvider putAndContinue(String key, Object val) {
		map.put(key, val);
		return this;
	}

	/**
	 * desired property must be available
	 *
	 * @param key
	 *            desired property name
	 * @return integer value
	 */
	public Integer getIntSafe(String key) {
		return Integer.valueOf(map.get(key).toString());
	}

	public Integer getInt(String key) {
		return map.containsKey(key) ? getIntSafe(key) : null;
	}

	public String getString(String key) {
		return map.containsKey(key) ? map.get(key).toString() : null;
	}

	public Boolean getBoolean(String key) {
		return map.containsKey(key) ? Boolean.valueOf(map.get(key).toString()) : null;
	}

	/**
	 * Desired specific property defined by object
	 * 
	 * @param key
	 *            property name
	 * @param <T>
	 *            object type
	 * @return object
	 */
	@SuppressWarnings("unchecked")
	public <T> T getPropertySafe(String key) {
		return (T) map.get(key);
	}

	@Override
	public String toString() {
		return "PropertiesProvider{" + "map=" + map + '}';
	}
}
