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
package com.robo4j.core;

/**
 * Exception thrown when a problem occurs configuring a RoboUnit.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ConfigurationException extends Exception {
	public ConfigurationException(String message) {
		super(message);
	}
	
	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public static ConfigurationException createMissingConfigNameException(String configName) {
		return new ConfigurationException(String.format("Unit requires target %s to be configured.", configName));
	}
	
}
