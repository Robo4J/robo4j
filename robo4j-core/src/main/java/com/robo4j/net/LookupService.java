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
package com.robo4j.net;

import com.robo4j.RoboContext;

import java.io.IOException;
import java.util.Map;

/**
 * A lookup service for discovery and lookup of remote {@link RoboContext}s.
 * These exist outside of any context. In normal use, there will be one per JVM.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface LookupService {
	/**
	 * @return an immutable Map of the currently discovered contexts.
	 */
	Map<String, RoboContextDescriptor> getDiscoveredContexts();

	/**
	 * Looks up a remote reference to a {@link RoboContext}.
	 * 
	 * @param id
	 *            the id of the context to lookup.
	 * @return a remote reference to a {@link RoboContext}. This will never be a
	 *         local context - if you find and lookup a reference to yourself,
	 *         messages sent will be passed over the network.
	 */
	RoboContext getContext(String id);

	/**
	 * Returns the descriptor for the specified id, if one is on record. Returns
	 * null if no context with the specified id is known.
	 * 
	 * @param id
	 *            the UUID of the context to look for.
	 * @return the descriptor for the specified id.
	 */
	RoboContextDescriptor getDescriptor(String id);

	/**
	 * Starts listening for information.
	 *
	 * @throws IOException
	 *             possible exception
	 */
	void start() throws IOException;

	/**
	 * Stops listening for information.
	 *
	 * @throws IOException
	 *             possible exception
	 */
	void stop() throws IOException;
}
