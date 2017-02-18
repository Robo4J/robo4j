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
package com.robo4j.core;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Future;

import com.robo4j.core.configuration.Configuration;

/**
 * Reference to a RoboUnit.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface RoboReference<T> {
	/**
	 * Sends a message to this RoboUnit.
	 * 
	 * @param message
	 *            the message to send.
	 * @return the RoboUnit specific response.
	 */
	void sendMessage(T message);

	/**
	 * @return the configuration.
	 */
	Configuration getConfiguration();

	/**
	 * @return the value for a certain attribute.
	 */
	<R> Future<R> getAttribute(AttributeDescriptor<R> attribute);

	/**
	 * @return the attributes that this RoboUnit knows about. Should not change,
	 *         and AttributeDescriptiors may be cached.
	 */
	Collection<AttributeDescriptor<?>> getKnownAttributes();

	/**
	 * @return the values of all attributes in a go, providing the state of the
	 *         RoboUnit.
	 */
	Future<Map<AttributeDescriptor<?>, Object>> getAttributes();
}
