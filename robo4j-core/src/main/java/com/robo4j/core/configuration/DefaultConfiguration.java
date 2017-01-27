/*
 * Copyright (c) 2014, 2017, Miroslav Wengner, Marcus Hirt
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
package com.robo4j.core.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of a {@link Configuration}.
 * 
 * <p>Internal Use Only</p>
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 * @since 10.01.2017
 */
class DefaultConfiguration implements Configuration {
	private final Map<String, Object> settings = new HashMap<>();
	private final Map<String, Configuration> configurations = new HashMap<>();

	@Override
	public Configuration createChildConfiguration(String name) {
		DefaultConfiguration config = new DefaultConfiguration();
		configurations.put(name, config);
		return config;
	}

	@Override
	public Configuration getChildConfiguration(String name) {
		return configurations.get(name);
	}

	@Override
	public double getDouble(String name) {
		return (double) settings.get(name);
	}

	@Override
	public long getLong(String name) {
		return (long) settings.get(name);
	}

	@Override
	public String getString(String name) {
		return (String) settings.get(name);
	}

	@Override
	public void setString(String name, String s) {
		settings.put(name, s);
	}

	@Override
	public void setLong(String name, long l) {
		settings.put(name, l);
	}

	@Override
	public void setDouble(String name, double d) {
		settings.put(name, d);
	}

	@Override
	public void setInt(String name, int i) {
		settings.put(name, i);
	}

	@Override
	public void setFloat(String name, float f) {
		settings.put(name, f);
	}

	@Override
	public int getInt(String name) {
		return (int) settings.get(name);
	}

	@Override
	public float getFloat(String name) {
		return (float) settings.get(name);
	}

	@Override
	public Set<String> getValueNames() {
		return settings.keySet();
	}

	@Override
	public Set<String> getChildNames() {
		return configurations.keySet();
	}

	@Override
	public Object getValue(String name) {
		return settings.get(name);
	}

	@Override
	public boolean getBoolean(String name) {
		return (boolean) settings.get(name);
	}

	@Override
	public void setBoolean(String name, boolean b) {
		settings.put(name, b);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configurations == null) ? 0 : configurations.hashCode());
		result = prime * result + ((settings == null) ? 0 : settings.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultConfiguration other = (DefaultConfiguration) obj;
		if (configurations == null) {
			if (other.configurations != null)
				return false;
		} else if (!configurations.equals(other.configurations))
			return false;
		if (settings == null) {
			if (other.settings != null)
				return false;
		} else if (!settings.equals(other.settings))
			return false;
		return true;
	}

}
