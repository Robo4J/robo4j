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
import java.util.Collections;
import java.util.concurrent.Future;

import com.robo4j.core.configuration.Configuration;

/**
 * The core component. Subclass this to provide a messaging capable agent for a
 * robot component.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public abstract class RoboUnit<T> implements RoboReference<T> {
	private final RoboContext context;
	private final String id;
	private volatile LifecycleState state = LifecycleState.UNINITIALIZED;
	private RoboReference<T> reference;
	private Configuration configuration;

	/**
	 * Either provide id up front
	 */
	public RoboUnit(RoboContext context, String id) {
		this.context = context;
		this.id = id;
		if (context instanceof RoboSystem) {
			reference = ((RoboSystem) context).getReference(this);
		}
	}

	/**
	 * @return the {@link RoboSystem} unique identifier for this unit.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the {@link RoboContext} associated with this unit.
	 */
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
	public <R> RoboResult<T, R> onMessage(T message) {
		return null;
	}

	/**
	 * If initializing the unit programmatically, call unit with the proper
	 * configuration.
	 * 
	 * @param configuration
	 *            the {@link Configuration} provided.
	 * @throws Exception
	 */
	public void initialize(Configuration configuration) throws ConfigurationException {
		setConfiguration(configuration);
		onInitialization(configuration);
		setState(LifecycleState.INITIALIZED);
	}

	/**
	 * Should be implemented by subclasses to do the actual Unit specific part
	 * of the initialization.
	 * 
	 * @param configuration
	 *            the {@link Configuration} provided.
	 * @throws ConfigurationException
	 */
	protected abstract void onInitialization(Configuration configuration) throws ConfigurationException;

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

	@Override
	public Configuration getConfiguration() {
		return configuration;
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
	public <R> Future<RoboResult<T, R>> sendMessage(T message) {
		return reference.sendMessage(message);
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

	/**
	 * @return a RoboReference. Internal use only.
	 */
	RoboReference<T> internalGetReference() {
		// NOTE(Marcus/Jan 27, 2017): Can we avoid this?
		if (reference == null) {
			return getContext().getReference(getId());
		} else {
			return reference;
		}
	}

	private void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
}
