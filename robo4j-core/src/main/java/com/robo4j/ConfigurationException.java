/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j;

/**
 * Exception thrown when a problem occurs configuring a RoboUnit.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ConfigurationException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            the exception message.
	 */
	public ConfigurationException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            the exception message.
	 * @param cause
	 *            the cause (@see {@link #getCause()})
	 */
	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Factory method to create a ConfigurationException for when a mandatory
	 * configuration setting is missing.
	 * 
	 * @param configName
	 *            the name of the missing configuration entry.
	 * @return the ConfigurationException with a properly formatted message.
	 */
	public static ConfigurationException createMissingConfigNameException(String configName) {
		return new ConfigurationException(String.format("Unit requires %s to be configured.", configName));
	}

}
