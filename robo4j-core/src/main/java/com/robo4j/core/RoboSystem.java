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
import java.util.stream.Collectors;

import com.robo4j.core.concurrency.RoboThreadFactory;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.scheduler.DefaultScheduler;
import com.robo4j.core.scheduler.Scheduler;

/**
 * This is the default implementation for a local {@link RoboContext}. Contains
 * RoboUnits, a lookup service for references to RoboUnits, and a system level
 * life cycle.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboSystem implements RoboContext {
	private static final int DEFAULT_THREAD_POOL_SIZE = 2;
	private static final int TERMINATION_TIMEOUT = 5;
	private static final int KEEP_ALIVE_TIME = 10;
	private volatile AtomicReference<LifecycleState> state = new AtomicReference<>(LifecycleState.UNINITIALIZED);
	private final Map<String, RoboUnit<?>> units = new HashMap<>();
	private final Map<RoboUnit<?>, RoboReference<?>> referenceCache = new WeakHashMap<>();

	private final ThreadPoolExecutor systemExecutor;
	private final Scheduler scheduler = new DefaultScheduler(this);
	private final LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
	private final String uid = UUID.randomUUID().toString();

	private class LocalRoboReference<T> implements RoboReference<T> {
		private final RoboUnit<T> unit;

		LocalRoboReference(RoboUnit<T> unit) {
			this.unit = unit;
		}

		@Override
		public String getId() {
			return unit.getId();
		}

		@Override
		public LifecycleState getState() {
			return unit.getState();
		}

		@Override
		public Configuration getConfiguration() {
			return unit.getConfiguration();
		}

		@Override
		public void sendMessage(T message) {
			if (getState() == LifecycleState.STARTED) {
				systemExecutor.submit(() -> unit.onMessage(message));
			}
		}

		@Override
		public <R> Future<R> getAttribute(AttributeDescriptor<R> attribute) {
			return systemExecutor.submit(() -> unit.onGetAttribute(attribute));
		}

		@Override
		public Collection<AttributeDescriptor<?>> getKnownAttributes() {
			return unit.getKnownAttributes();
		}

		@Override
		public Future<Map<AttributeDescriptor<?>, Object>> getAttributes() {
			return systemExecutor.submit(() -> unit.onGetAttributes());
		}

		@Override
		public Class<T> getMessageType() {
			return unit.getMessageType();
		}
	}

	/**
	 * Constructor.
	 */
	public RoboSystem() {
		this(DEFAULT_THREAD_POOL_SIZE);
	}

	/**
	 * Constructor.
	 * 
	 * @param threadPoolSize
	 *            the size of the messaging thread pool.
	 */
	public RoboSystem(int threadPoolSize) {
		systemExecutor = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, KEEP_ALIVE_TIME, TimeUnit.SECONDS, workQueue,
				new RoboThreadFactory("Robo4J System", true));
	}

	/**
	 * Constructor.
	 * 
	 * @param threadPoolSize
	 *            the size of the messaging thread pool.
	 * @param unitSet
	 *            a set of units to add to the system.
	 */
	public RoboSystem(int threadPoolSize, Set<RoboUnit<?>> unitSet) {
		this(threadPoolSize);
		addToMap(unitSet);
	}

	/**
	 * Adds the specified units to the system.
	 * 
	 * @param unitSet
	 *            the units to add.
	 */
	public void addUnits(Set<RoboUnit<?>> unitSet) {
		if (state.get() != LifecycleState.UNINITIALIZED) {
			throw new UnsupportedOperationException("All units must be registered up front for now.");
		}
		addToMap(unitSet);
	}

	/**
	 * Adds the specified units to the system.
	 * 
	 * @param units
	 *            the units to add.
	 */
	public void addUnits(RoboUnit<?>... units) {
		if (state.get() != LifecycleState.UNINITIALIZED) {
			throw new UnsupportedOperationException("All units must be registered up front for now.");
		}
		addToMap(units);
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
			systemExecutor.awaitTermination(TERMINATION_TIMEOUT, TimeUnit.SECONDS);
			systemExecutor.shutdown();
			scheduler.shutdown();
		} catch (InterruptedException e) {
			SimpleLoggingUtil.error(getClass(), "Was interrupted when shutting down.", e);
		}
		state.set(LifecycleState.SHUTTING_DOWN);
		units.values().forEach(RoboSystem::shutdownUnit);
		state.set(LifecycleState.SHUTDOWN);
	}

	@Override
	public LifecycleState getState() {
		return state.get();
	}

	@Override
	public Collection<RoboReference<?>> getUnits() {
		return units.values().stream().map(unit -> getReference(unit)).collect(Collectors.toList());
	}

	@Override
	public <T> RoboReference<T> getReference(String id) {
		@SuppressWarnings("unchecked")
		RoboUnit<T> roboUnit = (RoboUnit<T>) units.get(id);
		if (roboUnit == null) {
			return null;
		}
		return getReference(roboUnit);
	}

	@Override
	public Scheduler getScheduler() {
		return scheduler;
	}

	@Override
	public String getId() {
		return uid;
	}

	/**
	 * Returns the reference for a specific unit.
	 * 
	 * @param roboUnit
	 *            the robo unit for which to retrieve a reference.
	 * @return the {@link RoboReference} to the unit.
	 */
	public <T> RoboReference<T> getReference(RoboUnit<T> roboUnit) {
		@SuppressWarnings("unchecked")
		RoboReference<T> reference = (RoboReference<T>) referenceCache.get(roboUnit);
		if (reference == null) {
			reference = createReference(roboUnit);
			referenceCache.put(roboUnit, reference);
		}
		return reference;
	}

	private <T> RoboReference<T> createReference(RoboUnit<T> roboUnit) {
		return new LocalRoboReference<>(roboUnit);
	}

	private void addToMap(Set<RoboUnit<?>> unitSet) {
		unitSet.forEach(unit -> units.put(unit.getId(), unit));
	}

	private void addToMap(RoboUnit<?>... unitArray) {
		// NOTE(Marcus/Aug 9, 2017): Do not streamify...
		for (RoboUnit<?> unit : unitArray) {
			units.put(unit.getId(), unit);
		}
	}

	private static void shutdownUnit(RoboUnit<?> unit) {
		unit.setState(LifecycleState.SHUTTING_DOWN);
		// NOTE(Marcus/Aug 11, 2017): Should really be scheduled and done in
		// parallel.
		unit.shutdown();
	}
}
