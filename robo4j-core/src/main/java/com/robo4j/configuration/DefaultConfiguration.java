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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of a {@link Configuration}.
 * 
 * <p>
 * Internal Use Only
 * </p>
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class DefaultConfiguration implements Configuration {
	private static final long serialVersionUID = 1L;
	private final Map<String, Object> settings = new HashMap<>();
	private final Map<String, Configuration> configurations = new HashMap<>();

	@Override
	public Configuration getChildConfiguration(String name) {
		return configurations.get(name);
	}

	@Override
	public Double getDouble(String name, Double defaultValue) {
		return (Double) getVal(name, defaultValue);
	}

	@Override
	public Long getLong(String name, Long defaultValue) {
		return (Long) getVal(name, defaultValue);
	}

	@Override
	public String getString(String name, String defaultValue) {
		return (String) getVal(name, defaultValue);
	}

	public void setString(String name, String s) {
		settings.put(name, s);
	}

	@Override
	public Character getCharacter(String name, Character defaultValue) {
		return (Character) getVal(name, defaultValue);
	}

	@Override
	public Integer getInteger(String name, Integer defaultValue) {
		return (Integer) getVal(name, defaultValue);
	}

	@Override
	public Float getFloat(String name, Float defaultValue) {
		return (Float) getVal(name, defaultValue);
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
	public Object getValue(String name, Object defaultValue) {
		return getVal(name, defaultValue);
	}

	@Override
	public Boolean getBoolean(String name, Boolean defaultValue) {
		return (Boolean) getVal(name, defaultValue);
	}
	
	public Configuration createChildConfiguration(String name) {
		DefaultConfiguration config = new DefaultConfiguration();
		configurations.put(name, config);
		return config;
	}
	
	public void setBoolean(String name, Boolean b) {
		settings.put(name, b);
	}
	
	public void setCharacter(String name, Character s) {
		settings.put(name, s);
	}

	public void setLong(String name, Long l) {
		settings.put(name, l);
	}

	public void setDouble(String name, Double d) {
		settings.put(name, d);
	}

	public void setInteger(String name, Integer i) {
		settings.put(name, i);
	}

	public void setFloat(String name, Float f) {
		settings.put(name, f);
	}


	private Object getVal(String name, Object defaultValue) {
		Object val = settings.get(name);
		if (val == null) {
			return defaultValue;
		}
		return val;
	}

	// TODO : we may have it wrong here hashCode, equals
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

	@Override
	public String toString() {
		return "Settings: " + settings.toString() + " Configurations: " + configurations.toString();
	}
	
	/*
	 * Package local, to be used by the builder.
	 */
	void addChildConfiguration(String name, Configuration config) {
		configurations.put(name, config);
	}
}
