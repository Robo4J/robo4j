/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This ProcessAgent.java  is part of robo4j.
 *
 *  robo4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  robo4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.commons.agent;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.robo4j.commons.command.RoboTypeCommand;

/**
 * ProcessAgent is currently designed for RoboFrontHand
 *
 * it contains command process method which takes Function. The function has as
 * the imput specific command type
 *
 *
 * @author Miro Wengner (@miragemiko)
 * @since 30.06.2016
 */
public class ProcessAgent<RoboCommand extends RoboTypeCommand> implements DefaultAgent {

	private final AgentCache<AgentStatus> cache;
	private volatile AtomicBoolean active;
	private ExecutorService executor;
	private String name;
	private AgentProducer producer;
	private AgentConsumer consumer;

	public ProcessAgent() {
		this.active = new AtomicBoolean(false);
		this.cache = new AgentCache<>();
	}

	public AgentStatus process(RoboCommand command, Function<RoboCommand, AgentStatus> function) {
		if (Objects.nonNull(command)) {
			final AgentStatus result = function.apply(command);
			cache.put(result);
			switch (result.getStatus()) {
			case ACTIVE:
				active.set(true);
				break;
			case OFFLINE:
				active.set(false);
				break;
			default:
				throw new AgentException("PROCESS agentStatus = " + result);
			}
			return result;
		} else {
			return null;
		}
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	@Override
	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	public AgentProducer getProducer() {
		return producer;
	}

	@Override
	public void setProducer(AgentProducer producer) {
		this.producer = producer;
	}

	public AgentConsumer getConsumer() {
		return consumer;
	}

	@Override
	public void setConsumer(AgentConsumer consumer) {
		this.consumer = consumer;
	}

	public boolean getActive() {
		return active.get();
	}

	public void setActive(boolean active) {
		this.active.set(active);
	}
}
