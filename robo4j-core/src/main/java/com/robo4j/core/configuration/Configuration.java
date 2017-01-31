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

import java.util.Set;

/**
 * The configuration for a RoboUnit.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 * @since 10.01.2017
 */
public interface Configuration {
	Configuration createChildConfiguration(String name);
	Configuration getChildConfiguration(String name);

	Set<String> getChildNames();

	Boolean getBoolean(String name, Boolean defaultValue);
	Double getDouble(String name, Double defaultValue);
	Float getFloat(String string, Float defaultValue);
	Integer getInteger(String string, Integer defaultValue);
	Long getLong(String name, Long defaultValue);
	String getString(String name, String defaultValue);
	Character getCharacter(String name, Character character);
	Object getValue(String name, Object defaultValue);
	
	Set<String> getValueNames();
	void setBoolean(String name, Boolean b);
	void setDouble(String string, Double d);
	void setFloat(String string, Float f);
	void setInteger(String string, Integer i);
	void setLong(String string, Long l);
	void setCharacter(String string, Character c);
	void setString(String string, String s);
}
