/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This DefaultUnit.java  is part of robo4j.
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

package com.robo4j.commons.unit;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.robo4j.commons.agent.AgentConsumer;
import com.robo4j.commons.agent.AgentProducer;
import com.robo4j.commons.agent.AgentStatus;
import com.robo4j.commons.agent.GenericAgent;
import com.robo4j.commons.agent.ProcessAgent;
import com.robo4j.commons.command.RoboUnitCommand;

/**
 * DefaultUnit represent basic structure of the RoboUnit annotated classes
 *
 * @author Miro Wengner (@miragemiko)
 * @since 29.06.2016
 */
public abstract class DefaultUnit implements GenericUnit {

	/* RoboUnit has been successfully activated */
	protected volatile AtomicBoolean active;

	protected ExecutorService executorForAgents;
	/* Each RoboUnit has RoboAgents */
	protected List<GenericAgent> agents;

	protected Map<RoboUnitCommand, Function<ProcessAgent, AgentStatus>> logic;

	protected abstract GenericAgent createAgent(String name, AgentProducer producer, AgentConsumer consumer);

	public abstract Map<RoboUnitCommand, Function<ProcessAgent, AgentStatus>> initLogic();

	public List<GenericAgent> getAgents() {
		return this.agents;
	}

}
