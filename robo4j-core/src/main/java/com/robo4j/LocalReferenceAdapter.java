/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Future;

import com.robo4j.configuration.Configuration;

/**
 * This is a useful adapter to hand off a RoboReference to a unit which will
 * only use it as a callback. Note that this has several serious limits. First,
 * it must only be sent to a local unit. Secondly, you must know that the
 * RoboReference will only used to pass on a message. Thirdly, the message will
 * be handled in the same thread as the thread handling the message calling the
 * reference. Never block or spend much time in the onMessage method.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public abstract class LocalReferenceAdapter<T> implements RoboReference<T> {
	private final Class<T> clazz;

	public LocalReferenceAdapter(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public <R> Future<R> getAttribute(AttributeDescriptor<R> arg0) {
		return null;
	}

	@Override
	public Future<Map<AttributeDescriptor<?>, Object>> getAttributes() {
		return null;
	}

	@Override
	public Configuration getConfiguration() {
		return null;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public Collection<AttributeDescriptor<?>> getKnownAttributes() {
		return null;
	}

	@Override
	public Class<T> getMessageType() {
		return clazz;
	}

	@Override
	public LifecycleState getState() {
		return LifecycleState.STARTED;
	}
}
