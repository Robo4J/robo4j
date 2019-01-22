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
package com.robo4j;

import com.robo4j.configuration.Configuration;
import com.robo4j.scheduler.Scheduler;

import java.util.Collection;

/**
 * The execution context available for a unit. Contains a simple lookup service,
 * and life cycle management. Can be thought of as a system reference.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface RoboContext {
	/**
	 * Returns the current state of the system represented by the context.
	 * 
	 * @return the state of the system.
	 */
	LifecycleState getState();

	/**
	 * Shuts down the system. There is no returning from this.
	 */
	void shutdown();

	/**
	 * Stops (suspends) the system. Can be started again with start.
	 */
	void stop();

	/**
	 * Starts the system. Can be stopped (suspended) again with stop.
	 */
	void start();

	/**
	 * Returns a reference to a specific robo unit.
	 * 
	 * @param id
	 *            the unique id of the robo unit for which to get a reference.
	 * @param <T>
	 *            RoboReference
	 * @return the reference to the robo unit.
	 */
	<T> RoboReference<T> getReference(String id);

	/**
	 * Returns the units available in the context.
	 * 
	 * @return the available units.
	 */
	Collection<RoboReference<?>> getUnits();

	/**
	 * Returns the system scheduler.
	 * 
	 * @return the system scheduler.
	 */
	Scheduler getScheduler();

	/**
	 * Returns the globally unique id for the context.
	 * 
	 * @return the globally unique id for the context.
	 */
	String getId();

	/**
	 * Metadata describing the system.
	 *
	 * @return configuration
	 */
	Configuration getConfiguration();
}
