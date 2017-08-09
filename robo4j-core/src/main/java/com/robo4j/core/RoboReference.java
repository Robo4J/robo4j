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
	 * Returns the {@link RoboContext} unique identifier for this unit.
	 * 
	 * @return the id for the unit.
	 */
	String getId();

	/**
	 * Returns the life cycle state (@see {@link LifecycleState}) of this unit.
	 * 
	 * @return the state in the life cycle of this unit.
	 */
	LifecycleState getState();

	/**
	 * Sends a message to this RoboUnit.
	 * 
	 * @param message
	 *            the message to send.
	 */
	void sendMessage(T message);

	/**
	 * Returns the type of messages this RoboUnit accepts. This should never
	 * change in runtime.
	 * 
	 * @return the type of messages this RoboUnit accepts.
	 */
	Class<T> getMessageType();

	/**
	 * Returns the configuration for this RoboUnit.
	 * 
	 * @return the configuration.
	 */
	Configuration getConfiguration();

	/**
	 * Returns the value of the specified attribute.
	 * 
	 * @param attribute
	 *            the attribute to read.
	 * 
	 * @return the value of the attribute.
	 */
	<R> Future<R> getAttribute(AttributeDescriptor<R> attribute);

	/**
	 * Returns the attributes that this RoboUnit knows about. Should not change,
	 * and AttributeDescriptors may be cached.
	 * 
	 * @return the known attributes.
	 */
	Collection<AttributeDescriptor<?>> getKnownAttributes();

	/**
	 * Returns the values of all attributes in one read.
	 * 
	 * @return the values of all attributes.
	 */
	Future<Map<AttributeDescriptor<?>, Object>> getAttributes();
}
