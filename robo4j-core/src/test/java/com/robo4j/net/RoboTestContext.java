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
package com.robo4j.net;

import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.scheduler.Scheduler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

public class RoboTestContext implements RoboContext {
	private final String id;
	private final Map<String, RoboReference<?>> referenceMap = new HashMap<>();
	private final Configuration configuration;

	public RoboTestContext(String id, Configuration configuration) {
		this.id = id;
		this.configuration = configuration;
	}
	
	@Override
	public LifecycleState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> RoboReference<T> getReference(String id) {
		return (RoboReference<T>) referenceMap.get(id);
	}

	@Override
	public Collection<RoboReference<?>> getUnits() {
		return referenceMap.values();
	}

	@Override
	public Scheduler getScheduler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	public void addRef(RoboTestReference roboTestReference) {
		referenceMap.put(roboTestReference.getId(), roboTestReference);
	}

}
