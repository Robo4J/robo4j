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

import java.io.Serializable;
import java.util.Set;

/**
 * Typed configuration, for example used when configuring RoboUnits.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface Configuration extends Serializable {
	/**
	 * Creates a child configuration under this configuration, with the
	 * specified name.
	 * 
	 * @param name
	 *            the name of the child configuration.
	 * @return the child configuration which was created.
	 */
	Configuration createChildConfiguration(String name);

	/**
	 * Returns the child configuration with the specified name.
	 * 
	 * @param name
	 *            the name of the child configuration to retrieve.
	 * @return the named child configuration.
	 */
	Configuration getChildConfiguration(String name);

	/**
	 * Returns the names of the child configurations available for this
	 * configuration.
	 * 
	 * @return the names of the child configurations available for this
	 *         configuration.
	 */
	Set<String> getChildNames();

	/**
	 * Returns the names for the values available for this configuration.
	 * 
	 * @return the names for the values available for this configuration
	 */
	Set<String> getValueNames();

	/**
	 * Returns the Boolean stored under the specified name.
	 * 
	 * @param name
	 *            the name of the Boolean to retrieve.
	 * @param defaultValue
	 *            the default value if no value is set. Use null if you wish to
	 *            discover that no value was set.
	 * @return the value stored under the specified name, or defaultValue if no
	 *         such value could be found.
	 */
	Boolean getBoolean(String name, Boolean defaultValue);

	/**
	 * Returns the Double stored under the specified name.
	 * 
	 * @param name
	 *            the name of the Double to retrieve.
	 * @param defaultValue
	 *            the default value if no value is set. Use null if you wish to
	 *            discover that no value was set.
	 * @return the value stored under the specified name, or defaultValue if no
	 *         such value could be found.
	 */
	Double getDouble(String name, Double defaultValue);

	/**
	 * Returns the Float stored under the specified name.
	 *
	 * @param name
	 *            the name of the Float to retrieve.
	 * @param defaultValue
	 *            the default value if no value is set. Use null if you wish to
	 *            discover that no value was set.
	 * @return the value stored under the specified name, or defaultValue if no
	 *         such value could be found.
	 */
	Float getFloat(String name, Float defaultValue);

	/**
	 * Returns the Integer stored under the specified name.
	 * 
	 * @param name
	 *            the name of the Integer to retrieve.
	 * @param defaultValue
	 *            the default value if no value is set. Use null if you wish to
	 *            discover that no value was set.
	 * @return the value stored under the specified name, or defaultValue if no
	 *         such value could be found.
	 */
	Integer getInteger(String name, Integer defaultValue);

	/**
	 * Returns the Long stored under the specified name.
	 * 
	 * @param name
	 *            the name of the Long to retrieve.
	 * @param defaultValue
	 *            the default value if no value is set. Use null if you wish to
	 *            discover that no value was set.
	 * @return the value stored under the specified name, or defaultValue if no
	 *         such value could be found.
	 */
	Long getLong(String name, Long defaultValue);

	/**
	 * Returns the String stored under the specified name.
	 * 
	 * @param name
	 *            the name of the String to retrieve.
	 * @param defaultValue
	 *            the default value if no value is set. Use null if you wish to
	 *            discover that no value was set.
	 * @return the value stored under the specified name, or defaultValue if no
	 *         such value could be found.
	 */
	String getString(String name, String defaultValue);

	/**
	 * Returns the Character stored under the specified name.
	 * 
	 * @param name
	 *            the name of the Character to retrieve.
	 * @param character
	 *            the default value if no value is set. Use null if you wish to
	 *            discover that no value was set.
	 * @return the value stored under the specified name, or defaultValue if no
	 *         such value could be found.
	 */
	Character getCharacter(String name, Character character);

	/**
	 * General getter to retrieve any named value. This method will only return an instance of a
	 * type of the other supported getter types. This method is normally used
	 * together with {@link #getChildNames()}.
	 * 
	 * @param name
	 *            the name of the configuration value to retrieve.
	 * @param defaultValue
	 *            the default value retrieved.
	 * @return the value stored under the specified name, or defaultValue if no
	 *         such value could be found.
	 */
	Object getValue(String name, Object defaultValue);

	/**
	 * Stores the Boolean value b under the specified name.
	 * 
	 * @param name
	 *            the name under which to store the value.
	 * @param b
	 *            the Boolean to store.
	 */
	void setBoolean(String name, Boolean b);

	/**
	 * Stores the Double value d under the specified name.
	 * 
	 * @param name
	 *            the name under which to store the value.
	 * @param d
	 *            the Double to store.
	 */
	void setDouble(String name, Double d);

	/**
	 * Stores the Float value f under the specified name.
	 * 
	 * @param name
	 *            the name under which to store the value.
	 * @param f
	 *            the Float to store.
	 */
	void setFloat(String name, Float f);

	/**
	 * Stores the Integer value i under the specified name.
	 * 
	 * @param name
	 *            the name under which to store the value.
	 * @param i
	 *            the Integer to store.
	 */
	void setInteger(String name, Integer i);

	/**
	 * Stores the Long value l under the specified name.
	 * 
	 * @param name
	 *            the name under which to store the value.
	 * @param l
	 *            the Long to store.
	 */
	void setLong(String name, Long l);

	/**
	 * Stores the Character value c under the specified name.
	 * 
	 * @param name
	 *            the name under which to store the value.
	 * @param c
	 *            the Character to store.
	 */
	void setCharacter(String name, Character c);

	/**
	 * Stores the String value s under the specified name.
	 * 
	 * @param name
	 *            the name under which to store the value.
	 * @param s
	 *            the String to store.
	 */
	void setString(String name, String s);
}
