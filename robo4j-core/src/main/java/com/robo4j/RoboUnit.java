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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * The core component. Subclass this to provide a messaging capable agent for a
 * robot component.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public abstract class RoboUnit<T> implements RoboReference<T> {
	// Yay for erasure
	private final Class<T> messageType;
	private final RoboContext context;
	private final String id;
	private volatile LifecycleState state = LifecycleState.UNINITIALIZED;
	private RoboReference<T> reference;
	private Configuration configuration;

	/**
	 * Constructor.
	 *
	 * @param messageType
	 *            messageType
	 * @param context
	 *            desired Robo context
	 * @param id
	 *            id of RoboUnit
	 */
	public RoboUnit(Class<T> messageType, RoboContext context, String id) {
		this.messageType = messageType;
		this.context = context;
		this.id = id;
		if (context instanceof RoboSystem) {
			reference = ((RoboSystem) context).getReference(this);
		}
	}

	/**
	 * @return the {@link RoboSystem} unique identifier for this unit.
	 */
	@Override
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
	 * If initializing the unit programmatically, call unit with the proper
	 * configuration.
	 * 
	 * @param configuration
	 *            the {@link Configuration} provided.
	 * @throws ConfigurationException
	 *             possible exception
	 */
	public void initialize(Configuration configuration) throws ConfigurationException {
		setConfiguration(configuration);
		onInitialization(configuration);
		setState(LifecycleState.INITIALIZED);
	}

	/**
	 * Should be implemented by subclasses to do the actual Unit specific part of
	 * the initialization.
	 * 
	 * @param configuration
	 *            the {@link Configuration} provided.
	 * @throws ConfigurationException
	 *             possible exception
	 */
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
	}

	/**
	 * Should be overridden in subclasses which need to do some initialization on
	 * start.
	 */
	public void start() {
	}

	/**
	 * Should be overridden in subclasses needing to do some work when stopping.
	 */
	public void stop() {
	}

	/**
	 * Should be overridden in subclasses needing to do some work when shutting
	 * down.
	 */
	public void shutdown() {
	}

	/**
	 * Returns the state of this unit.
	 * 
	 * @return the state in the life cycle of this unit.
	 */
	@Override
	public LifecycleState getState() {
		return state;
	}

	/**
	 * It is considered good form to return the types that you can respond to. This
	 * method should be overriden in subclasses. Note that it is allowed for an
	 * agent to return the empty set. Returning null is not allowed.
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

	/**
	 * Sends a message to this unit by posting a message on the message bus.
	 * 
	 * @see #onMessage(Object)
	 */
	@Override
	public void sendMessage(T message) {
		reference.sendMessage(message);
	}

	/**
	 * Will post a message to get the attributes on the message queue.
	 * 
	 * @see #onGetAttributes()
	 */
	@Override
	public Future<Map<AttributeDescriptor<?>, Object>> getAttributes() {
		return reference.getAttributes();
	}

	/**
	 * Retrieves an attribute from this unit.
	 * 
	 * @see #getAttribute(AttributeDescriptor)
	 */
	@Override
	public <R> Future<R> getAttribute(AttributeDescriptor<R> attribute) {
		return reference.getAttribute(attribute);
	}

	/**
	 * Override in subclasses to expose the attributes known.
	 */
	@Override
	public Collection<AttributeDescriptor<?>> getKnownAttributes() {
		return Collections.emptyList();
	}

	@Override
	public Class<T> getMessageType() {
		return messageType;
	}

	/**
	 * Should be overridden in subclasses to define the behaviour of the unit. This
	 * method should normally not be called directly, unless you have a very good
	 * reason. It is used by the system to deliver messages to the unit.
	 * 
	 * @param message
	 *            the message received by this unit.
	 * 
	 */
	public void onMessage(T message) {
		// Note that this method is public so the scheduler has access. We may
		// want to consider other means of accessing it to keep it protected.
	}

	/**
	 * May be overridden in subclasses for more performance. The default
	 * implementation will get the job done though.
	 *
	 * @return the map of all the attributes.
	 */
	protected Map<AttributeDescriptor<?>, Object> onGetAttributes() {
		Map<AttributeDescriptor<?>, Object> result = new HashMap<>();
		Collection<AttributeDescriptor<?>> knownAttributes = getKnownAttributes();
		for (AttributeDescriptor<?> descriptor : knownAttributes) {
			result.put(descriptor, getAttribute(descriptor));
		}
		return result;
	}

	/**
	 * Should be overridden in subclasses to provide attributes.
	 *
	 * @param descriptor
	 *            the descriptor for which to return the attribute.
	 * @param <R>
	 *            attribute descriptor
	 * @return the attribute value.
	 */
	protected <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
		return null;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s [id=%s]", getClass().getName(), getId());
	}
}
