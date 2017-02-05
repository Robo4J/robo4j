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

import com.robo4j.core.scheduler.Scheduler;

/**
 * The execution context available for a unit. Contains a simple lookup service,
 * and life cycle management.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface RoboContext {
	/**
	 * @return the state of the system.
	 */
	LifecycleState getState();

	/**
	 * Shuts the entire system down. There is no return from this.
	 */
	void shutdown();

	/**
	 * Stops the system. Can be started again with start.
	 */
	void stop();

	/**
	 * Starts the system. Can be stopped again with stop.
	 */
	void start();

	/**
	 * Returns a reference to a specific RoboUnit.
	 * 
	 * @param id
	 * @return
	 */
	<T> RoboReference<T> getReference(String id);

	/**
	 * @return the available units.
	 */
	Collection<RoboUnit<?>> getUnits();

	/**
	 * @return the system scheduler.
	 */
	Scheduler getScheduler();
}
