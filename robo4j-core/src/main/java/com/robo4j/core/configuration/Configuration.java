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
 */
public interface Configuration {
	Configuration createChildConfiguration(String name);
	Configuration getChildConfiguration(String name);

	Set<String> getChildNames();

	boolean getBoolean(String name);
	double getDouble(String name);
	float getFloat(String string);
	int getInt(String string);
	long getLong(String name);
	String getString(String name);
	Object getValue(String name);
	
	Set<String> getValueNames();
	void setBoolean(String name, boolean b);
	void setDouble(String string, double d);
	void setFloat(String string, float f);
	void setInt(String string, int i);
	void setLong(String string, long l);
	void setString(String string, String s);
}
