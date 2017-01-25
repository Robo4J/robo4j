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
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * The core component. Subclass this to provide a messaging capable agent for a
 * robot component.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboUnit<T> implements RoboReference<T> {
	private final RoboContext context;
	private final String id;
	private volatile LifecycleState state = LifecycleState.UNINITIALIZED;
	private RoboReference<T> reference;

	/**
	 * Either provide id up front
	 */
	public RoboUnit(RoboContext context, String id) {
		this.context = context;
		this.id = id;
		if (context instanceof RoboSystem) { 
			reference = ((RoboSystem)context).getReference(this);
		}
	}

	public String getId() {
		return id;
	}

	public RoboContext getContext() {
		return context;
	}

	/**
	 * Should be overridden in subclasses to define the behaviour of the unit.
	 * 
	 * @param message
	 *            the message received by this unit.
	 * 
	 * @return the unit specific result from the call.
	 */
	public <R> RoboResult<T, R> onMessage(Object message) {
		return null;
	}

	public void initialize(Map<String, String> properties) throws Exception {
		setState(LifecycleState.INITIALIZED);
	}

	/**
	 * Should be overridden in subclasses which need to do some initialization
	 * on start.
	 * 
	 * <p>
	 * Default implementation sets state to {@link LifecycleState#STARTED}.
	 */
	public void start() {
		setState(LifecycleState.STARTED);
	}

	/**
	 * Should be overridden in subclasses needing to do some work when stopping.
	 * 
	 * <p>
	 * Default implementation sets state to {@link LifecycleState#STOPPED}.
	 */
	public void stop() {
		setState(LifecycleState.STOPPED);
	}

	/**
	 * Should be overridden in subclasses needing to do some work when shutting
	 * down.
	 * 
	 * <p>
	 * Default implementation sets state to {@link LifecycleState#SHUTDOWN}.
	 */
	public void shutdown() {
		setState(LifecycleState.SHUTDOWN);
	}

	/**
	 * Returns the state of this unit.
	 * 
	 * @return the state in the life cycle of this unit.
	 */
	public LifecycleState getState() {
		return state;
	}

	/**
	 * It is considered good form to return the types that you can respond to.
	 * This method should be overriden in subclasses. Note that it is allowed
	 * for an agent to return the empty set. Returning null is not allowed.
	 * 
	 * @return the message types accepted by this unit.
	 */
	public Collection<Class<?>> getAcceptedMessageTypes() {
		return Collections.emptySet();
	}

	/**
	 * Changes the {@link LifecycleState}.
	 * 
	 * @param state
	 *            the state to change to.
	 * 
	 * @see LifecycleState for allowable transitions.
	 */
	public void setState(LifecycleState state) {
		this.state = state;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s [id=%s]", getClass().getName(), getId());
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		RoboUnit<?> other = (RoboUnit<?>) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/**
	 * Sends a message to this unit.
	 */
	@Override
	public <R> Future<RoboResult<T, R>> sendMessage(Object message) {
		return reference.sendMessage(message);
	}
	
	public RoboReference<T> internalGetReference() {
		if (reference == null) {
			return getContext().getReference(getId());
		} else {
			return reference;
		}
	}
}
