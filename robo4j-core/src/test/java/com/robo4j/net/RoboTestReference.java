/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.net;

import com.robo4j.AttributeDescriptor;
import com.robo4j.LifecycleState;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

public class RoboTestReference implements RoboReference<String>{
	private String id;
	private final List<String> messages = new ArrayList<>();
	private final Configuration configuration;

	public RoboTestReference(String id, Configuration configuration) {
		this.id = id;
		this.configuration = configuration;
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public LifecycleState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendMessage(String message) {
		messages.add(message);
	}

	@Override
	public Class<String> getMessageType() {
		return String.class;
	}

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public <R> Future<R> getAttribute(AttributeDescriptor<R> attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<AttributeDescriptor<?>> getKnownAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<Map<AttributeDescriptor<?>, Object>> getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

}
