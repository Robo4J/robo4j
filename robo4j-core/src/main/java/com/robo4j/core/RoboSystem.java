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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import com.robo4j.core.concurrent.RoboSingleThreadFactory;
import com.robo4j.core.logging.SimpleLoggingUtil;

/**
 * Contains RoboUnits, RoboUnit lookup, a system level life cycle and a known
 * RoboUnit providing a system message queue.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboSystem implements RoboContext {
	public static final String ID_SYSTEM = "com.robo4j.core.system";
	private volatile AtomicReference<LifecycleState> state = new AtomicReference<>(LifecycleState.UNINITIALIZED);
	private final Map<String, RoboUnit<?>> units = new HashMap<>();
	private final Map<RoboUnit<?>, RoboReference<?>> referenceCache = new WeakHashMap<>();
	
	private final ThreadPoolExecutor systemExecutor;
	private final LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
	private final String uid = UUID.randomUUID().toString();
	
	private final RoboUnit<Object> systemUnit = new RoboUnit<>(this, ID_SYSTEM);

	private class ReferenceImplementation<T> implements RoboReference<T> {
		private final RoboUnit<T> unit;

		public ReferenceImplementation(RoboUnit<T> unit) {
			this.unit = unit;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Future<RoboResult<T, ?>> sendMessage(final Object message) {
			return systemExecutor.submit(() -> unit.onMessage(message));
		}
	}
	
	public RoboSystem() {
		units.put(ID_SYSTEM, systemUnit);
		systemExecutor = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS, workQueue,
				new RoboSingleThreadFactory("Robo4J System " + uid, true));
	}

	public RoboSystem(Set<RoboUnit<?>> unitSet) {
		this();
		addToMap(unitSet);
	}

	public void addUnits(Set<RoboUnit<?>> unitSet) {
		if (state.get() != LifecycleState.UNINITIALIZED) {
			throw new UnsupportedOperationException("All units must be registered up front for now.");
		}
		addToMap(unitSet);
	}

	public void addUnits(RoboUnit<?>... units) {
		if (state.get() != LifecycleState.UNINITIALIZED) {
			throw new UnsupportedOperationException("All units must be registered up front for now.");
		}
		addToMap(units);
	}

	public void addToMap(Set<RoboUnit<?>> unitSet) {
		unitSet.stream().forEach(unit -> units.put(unit.getId(), unit));
	}

	public void addToMap(RoboUnit<?>... unitArray) {
		Stream.of(unitArray).forEach(unit -> units.put(unit.getId(), unit));
	}

	@Override
	public void start() {
		state.set(LifecycleState.STARTING);
		units.values().forEach(RoboUnit::start);
		state.set(LifecycleState.STARTED);
	}

	@Override
	public void stop() {
		state.set(LifecycleState.STOPPING);
		units.values().forEach(RoboUnit::stop);
		state.set(LifecycleState.STOPPED);
	}

	@Override
	public void shutdown() {
		stop();
		try {
			systemExecutor.awaitTermination(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			SimpleLoggingUtil.error(getClass(), "Was interrupted when shutting down.", e);
		}
		state.set(LifecycleState.SHUTTING_DOWN);
		units.values().forEach(RoboUnit::shutdown);
		state.set(LifecycleState.SHUTDOWN);
	}

	@Override
	public LifecycleState getState() {
		return state.get();
	}

	/**
	 * Returns all the units in the system.
	 */
	public Collection<RoboUnit<?>> getUnits() {
		return units.values();
	}

	/**
	 * 
	 * @param id
	 * 
	 * @return returns the reference to the specified RoboUnit. The reference can be kept and 
	 */
	public <T> RoboReference<T> getReference(String id) {
		@SuppressWarnings("unchecked")
		RoboUnit<T> roboUnit = (RoboUnit<T>) units.get(id);
		if (roboUnit == null) {			
			return null;
		}
		return getReference(roboUnit);
	}

	private <T> RoboReference<T> createReference(RoboUnit<T> roboUnit) {
		return new ReferenceImplementation<>(roboUnit);
	}
	
	// NOTE(Marcus/Jan 24, 2017): We're only making sure that the reference is around, no more, no less.
	<T> RoboReference<T> getReference(RoboUnit<T> roboUnit) {
		@SuppressWarnings("unchecked")
		RoboReference<T> reference = (RoboReference<T>) referenceCache.get(roboUnit);
		if (reference == null) {
			reference = createReference(roboUnit);
			referenceCache.put(roboUnit, reference);
		}
		return reference;		
	}
}
