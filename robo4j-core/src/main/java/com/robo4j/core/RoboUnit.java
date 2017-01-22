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

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.robo4j.commons.logging.SimpleLoggingUtil;
import com.robo4j.core.concurrency.RoboSingleThreadFactory;

/**
 * The core component. Subclass this to provide a messaging capable agent for a
 * robot component.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboUnit<T> {
	private final RoboContext context;
	private final String id;
	private volatile LifecycleState state;

	// TODO(Marcus/Jan 21, 2017): Should limit size of queue, should add
	// monitoring.
	private final ThreadPoolExecutor outBox;
	private final LinkedBlockingQueue<Runnable> messageQueue = new LinkedBlockingQueue<>();

	/**
	 * Either provide id up front
	 */
	public RoboUnit(RoboContext context, String id) {
		this.context = context;
		this.id = id;

		outBox = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS, messageQueue,
				new RoboSingleThreadFactory("Robo4J Unit " + id, true));
	}

	public String getId() {
		return id;
	}

	public RoboContext getContext() {
		return context;
	}

	// FIXME(Marcus/Jan 21, 2017): Should be typed to T.
	public RoboResult<?> onMessage(Object message) {
		return null;
	}

	public Future<RoboResult<?>> sendMessage(final String targetId, Object message) {
		// FIXME(Marcus/Jan 22, 2017): Possibly remove this variant.
		return outBox.submit(() -> {
			return (RoboResult<?>) context.getRoboUnit(targetId).onMessage(message);
		});
	}

	public Future<RoboResult<?>> sendMessage(final RoboUnit<?> target, Object message) {
		return outBox.submit(() -> {
			return (RoboResult<?>) target.onMessage(message);
		});
	}

	public void initialize(Map<String, String> properties) throws Exception {
	}

	/**
	 * Should be overridden in subclasses which need to do some initialization
	 * on start.
	 * 
	 * <p>
	 * Default implementation sets state to {@link LifecycleState#STARTED}.
	 */
	protected void start() {
		setState(LifecycleState.STARTED);
	}

	/**
	 * Should be overridden in subclasses needing to do some work when stopping.
	 * 
	 * <p>
	 * Default implementation sets state to {@link LifecycleState#STOPPED}.
	 */
	protected void stop() {
		setState(LifecycleState.STOPPED);
	}

	/**
	 * Should be overridden in subclasses needing to do some work when shutting
	 * down.
	 * 
	 * <p>
	 * Default implementation sets state to {@link LifecycleState#SHUTDOWN}.
	 */
	protected void shutdown() {
		setState(LifecycleState.SHUTTING_DOWN);
		outBox.shutdown();
		try {
			outBox.awaitTermination(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			SimpleLoggingUtil.debug(getClass(), "Failed waiting for termination", e);
		}
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

	public void setState(LifecycleState state) {
		this.state = state;
	}

	public boolean hasUnprocessedMessages() {
		return messageQueue.size() > 0;
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
}
