/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This CommandProviderImpl.java  is part of robo4j.
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

package com.robo4j.core.system;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.robo4j.commons.agent.AgentConsumer;
import com.robo4j.commons.agent.AgentProducer;
import com.robo4j.commons.agent.AgentStatus;
import com.robo4j.commons.agent.GenericAgent;
import com.robo4j.commons.agent.ProcessAgent;
import com.robo4j.commons.agent.ProcessAgentBuilder;
import com.robo4j.commons.command.GenericCommand;
import com.robo4j.commons.command.RoboUnitCommand;
import com.robo4j.commons.concurrent.LegoThreadFactory;
import com.robo4j.commons.control.RoboSystemConfig;
import com.robo4j.commons.enums.RegistryTypeEnum;
import com.robo4j.commons.logging.SimpleLoggingUtil;
import com.robo4j.commons.registry.RegistryManager;
import com.robo4j.commons.registry.RoboRegistry;
import com.robo4j.commons.unit.DefaultUnit;
import com.robo4j.commons.unit.GenericUnit;
import com.robo4j.core.client.enums.RequestCommandEnum;
import com.robo4j.core.client.io.ClientException;
import com.robo4j.core.util.ConstantUtil;

/**
 *
 * Command Provider works like Unit Element Object
 *
 * @author Miro Wengner (@miragemiko)
 * @since 10.06.2016
 */
public class CommandProviderImpl extends DefaultUnit implements CommandProvider {

	private final List<GenericAgent> agents;
	private final List<GenericUnit> units;
	private volatile LinkedBlockingQueue<GenericCommand<RequestCommandEnum>> commandQueue;

	// TODO: Provider should get access to the all registered unit
	@SuppressWarnings(value = "unchecked")
	public CommandProviderImpl() {
		executorForAgents = Executors.newFixedThreadPool(ConstantUtil.PLATFORM_ENGINES,
				new LegoThreadFactory(ConstantUtil.PROVIDER_BUS));
		this.agents = new LinkedList<>();
		this.units = new LinkedList<>();
		this.active = new AtomicBoolean(false);

		this.commandQueue = new LinkedBlockingQueue<>();

		final RoboRegistry<RoboRegistry, RoboSystemConfig> unitRegistry = RegistryManager.getInstance()
				.getRegistryByType(RegistryTypeEnum.UNITS);
		unitRegistry.getRegistry().entrySet().forEach(entry -> {
			SimpleLoggingUtil.debug(getClass(), "UnitInit Name: " + entry.getKey());
			GenericUnit genericUnit = (GenericUnit) entry.getValue();
			genericUnit.setExecutor(executorForAgents);
			genericUnit.init(null);
			this.units.add(genericUnit);
			if (Objects.nonNull(genericUnit.getAgents()) && !genericUnit.getAgents().isEmpty()) {
				this.agents.addAll(genericUnit.getAgents());
			}
		});

		if (!agents.isEmpty()) {
			active.set(true);
		}
	}

	@Override
	public Object init(Object input) {
		SimpleLoggingUtil.error(getClass(), "init Called");
		return null;
	}

	@Override
	public void setExecutor(ExecutorService executor) {
		SimpleLoggingUtil.error(getClass(), "setExecutors Called");
	}

	@SuppressWarnings(value = "unchecked")
	@Override
	public boolean process(final GenericCommand<RequestCommandEnum> command) {
		SimpleLoggingUtil.debug(getClass(), "process: " + command);
		switch (command.getType().getTarget()) {
		case SYSTEM:
			return processSystemCommand(command);
		case PLATFORM:
			return processPlatformCommand(command);
		case HAND_UNIT:
			return processHandUnitCommand(command);
		case FRONT_UNIT:
			return processFrontUnitCommand(command);
		default:
			throw new ClientException("no such command target= " + command);
		}
	}

	// Protected Methods
	@Override
	protected GenericAgent createAgent(String name, AgentProducer producer, AgentConsumer consumer) {
		return Objects.nonNull(producer) && Objects.nonNull(consumer) ? ProcessAgentBuilder.Builder(executorForAgents)
				.setName(name).setProducer(producer).setConsumer(consumer).build() : null;
	}

	@Override
	public Map<RoboUnitCommand, Function<ProcessAgent, AgentStatus>> initLogic() {
		return null;
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public boolean process(RoboUnitCommand command) {
		return false;
	}

	@Override
	public String getUnitName() {
		return null;
	}

	@Override
	public String getSystemName() {
		return null;
	}

	@Override
	public String[] getProducerName() {
		return new String[0];
	}

	@Override
	public String getConsumerName() {
		return null;
	}

	// Private Methods
	/* currently system commad is executed as EXIT */
	private boolean processSystemCommand(final GenericCommand<RequestCommandEnum> command) {
		switch (command.getType()) {
		case EXIT:
			SimpleLoggingUtil.print(getClass(), "EXIT COMMAND HAS BEEN CALLED");
			active.set(false);
			executorForAgents.shutdown();
			return true;
		default:
			throw new ClientException("SYSTEM COMMAND= " + command);
		}
	}

	@SuppressWarnings(value = "unchecked")
	private boolean processPlatformCommand(final GenericCommand<RequestCommandEnum> command) {
		// TODO : more generic, problem is name
		SimpleLoggingUtil.print(getClass(), "getPlatform Unit units= " + units);
		return processUnitByName("platform", command);
	}

	@SuppressWarnings(value = "unchecked")
	private boolean processHandUnitCommand(final GenericCommand<RequestCommandEnum> command) {
		SimpleLoggingUtil.print(getClass(), "getFrontHand Unit units= " + units);
		return processUnitByName("frontHand", command);

	}

	private boolean processFrontUnitCommand(final GenericCommand<RequestCommandEnum> command) {
		SimpleLoggingUtil.debug(getClass(), "getFrontUnit Unit units= " + units);
		return processUnitByName("frontUnit", command);
	}

	private boolean processUnitByName(final String name, final GenericCommand<RequestCommandEnum> command) {
		Optional<GenericUnit> optUnit = units.stream().filter(u -> {
			SimpleLoggingUtil.print(CommandProviderImpl.class, "processUnitByName " + u.getUnitName());
			return u.getUnitName().contains(name);
		}).findFirst();

		if (optUnit.isPresent()) {
			SimpleLoggingUtil.debug(CommandProviderImpl.class, "UNIT IS PRESENT " + optUnit);
			return optUnit.get().process(command);
		} else {
			SimpleLoggingUtil.print(CommandProviderImpl.class, "NO PLATFORM for COMMAND: " + command);
			return false;
		}
	}

}
