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

import com.robo4j.commons.logging.SimpleLoggingUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * Contains RoboUnits, RoboUnit lookup, a system level life cycle and a known
 * RoboUnit providing a system message queue.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboSystem implements RoboContext {
	public static final String ID_SYSTEM = "com.robo4j.core.system";
	private volatile AtomicReference<LifecycleState> state = new AtomicReference<>(
			LifecycleState.UNINITIALIZED);
	private final Map<String, RoboUnit<?>> units = new HashMap<>();

	private final RoboUnit<Object> systemUnit = new RoboUnit<>(this, ID_SYSTEM);

	public RoboSystem() {
		SimpleLoggingUtil.debug(getClass(), "LOCALE= " + Locale.getDefault());
		units.put(ID_SYSTEM, systemUnit);
	}

	public RoboSystem(Set<RoboUnit<?>> unitSet) {
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
		state.set(LifecycleState.SHUTTING_DOWN);
		units.values().forEach(RoboUnit::shutdown);
		state.set(LifecycleState.SHUTDOWN);
	}

	@Override
	public LifecycleState getState() {
		return state.get();
	}

	@Override
	public RoboUnit<?> getRoboUnit(String id) {
		return units.get(id);
	}

	/**
	 * Sends a message on the system bus.
	 * 
	 * @param targetId
	 * @param message
	 * @return the response.
	 */
	public Future<RoboResult<?>> sendMessage(String targetId, Object message) {
		// FIXME(Marcus/Jan 22, 2017): Possibly remove this variant.
		return systemUnit.sendMessage(targetId, message);
	}

	/**
	 * Sends a message on the system bus.
	 * 
	 * @param target
	 * @param lcdMessage
	 * @return the response.
	 */
	public Future<RoboResult<?>> sendMessage(RoboUnit<?> target, Object message) {
		return systemUnit.sendMessage(target, message);
	}
}
