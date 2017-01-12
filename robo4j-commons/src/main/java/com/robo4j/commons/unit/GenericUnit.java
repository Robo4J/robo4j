/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This GenericUnit.java  is part of robo4j.
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
import java.util.function.Function;

import com.robo4j.commons.agent.AgentStatus;
import com.robo4j.commons.agent.GenericAgent;
import com.robo4j.commons.agent.ProcessAgent;
import com.robo4j.commons.command.RoboUnitCommand;
import com.robo4j.commons.control.RoboSystemConfig;

/**
 * GenericUnit is collection of intelligent agents
 *
 * @author Miro Wengner (@miragemiko)
 * @since 28.06.2016
 */
public interface GenericUnit extends RoboSystemConfig {

	void setExecutor(ExecutorService executor);

	boolean isActive();

	Object init(Object input);

	Map<RoboUnitCommand, Function<ProcessAgent, AgentStatus>> initLogic();

	List<GenericAgent> getAgents();

	boolean process(RoboUnitCommand command);

	String getUnitName();

	String getSystemName();

	String[] getProducerName();

	String getConsumerName();

}
