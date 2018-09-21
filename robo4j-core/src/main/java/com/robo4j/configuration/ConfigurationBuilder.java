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
package com.robo4j.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Helper to make it easier to build configurations programmatically.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ConfigurationBuilder {
	private DefaultConfiguration inProgress = new DefaultConfiguration();

	private Map<String, ConfigurationBuilder> children = new HashMap<>();

	/**
	 * Stores the Boolean value b under the specified name.
	 * 
	 * @param name
	 *            the name under which to store the value.
	 * @param b
	 *            the Boolean to store.
	 */
	public ConfigurationBuilder addBoolean(String name, Boolean b) {
		inProgress.setBoolean(name, b);
		return this;
	}

	/**
	 * Stores the Double value d under the specified name.
	 * 
	 * @param name
	 *            the name under which to store the value.
	 * @param d
	 *            the Double to store.
	 */
	public ConfigurationBuilder addDouble(String name, Double d) {
		inProgress.setDouble(name, d);
		return this;
	}

	/**
	 * Stores the Float value f under the specified name.
	 * 
	 * @param name
	 *            the name under which to store the value.
	 * @param f
	 *            the Float to store.
	 */
	public ConfigurationBuilder addFloat(String name, Float f) {
		inProgress.setFloat(name, f);
		return this;
	}

	/**
	 * Stores the Integer value i under the specified name.
	 * 
	 * @param name
	 *            the name under which to store the value.
	 * @param i
	 *            the Integer to store.
	 */
	public ConfigurationBuilder addInteger(String name, Integer i) {
		inProgress.setInteger(name, i);
		return this;
	}

	/**
	 * Stores the Long value l under the specified name.
	 * 
	 * @param name
	 *            the name under which to store the value.
	 * @param l
	 *            the Long to store.
	 */
	public ConfigurationBuilder addLong(String name, Long l) {
		inProgress.setLong(name, l);
		return this;
	}

	/**
	 * Stores the Character value c under the specified name.
	 * 
	 * @param name
	 *            the name under which to store the value.
	 * @param c
	 *            the Character to store.
	 */
	public ConfigurationBuilder addCharacter(String name, Character c) {
		inProgress.setCharacter(name, c);
		return this;
	}

	/**
	 * Stores the String value s under the specified name.
	 * 
	 * @param name
	 *            the name under which to store the value.
	 * @param s
	 *            the String to store.
	 */
	public ConfigurationBuilder addString(String name, String s) {
		inProgress.setString(name, s);
		return this;
	}

	public ConfigurationBuilder addBuilder(String name, ConfigurationBuilder builder) {
		children.put(name, builder);
		return this;
	}

	public Configuration build() {
		for (Entry<String, ConfigurationBuilder> entry : children.entrySet()) {
			inProgress.addChildConfiguration(entry.getKey(), entry.getValue().build());
		}
		return inProgress;
	}
}
