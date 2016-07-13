/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This CommandProviderImpl.java is part of robo4j.
 *
 *     robo4j is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     robo4j is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.robo4j.brick.system;

import com.robo4j.brick.client.enums.RequestCommandEnum;
import com.robo4j.brick.client.io.ClientException;
import com.robo4j.brick.unit.FrontHandUnit;
import com.robo4j.brick.unit.PlatformUnit;
import com.robo4j.brick.util.ConstantUtil;
import com.robo4j.commons.agent.AgentConsumer;
import com.robo4j.commons.agent.AgentProducer;
import com.robo4j.commons.agent.AgentStatus;
import com.robo4j.commons.agent.GenericAgent;
import com.robo4j.commons.agent.ProcessAgent;
import com.robo4j.commons.agent.ProcessAgentBuilder;
import com.robo4j.commons.command.GenericCommand;
import com.robo4j.commons.command.RoboUnitCommand;
import com.robo4j.commons.concurrent.LegoThreadFactory;
import com.robo4j.commons.unit.DefaultUnit;
import com.robo4j.lego.control.LegoEngine;
import com.robo4j.lego.control.LegoSensor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 *
 * Command Provider works like Unint Element Object
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 10.06.2016
 */
public class CommandProviderImpl extends DefaultUnit implements CommandProvider {

    private static final int AGENT_HAND_POSITION = 0;
    private static final int AGENT_PLATFORM_POSITION = 1;

    private volatile LinkedBlockingQueue<GenericCommand<RequestCommandEnum>> commandQueue;
    private final List<GenericAgent> agents;
    private final List<DefaultUnit> units;


    public CommandProviderImpl(Map<String, LegoEngine> engineCache,
                               Map<String, LegoSensor> sensorCache,
                               Map<String, DefaultUnit> unitCache) {
        executorForAgents =  Executors.newFixedThreadPool(ConstantUtil.PLATFORM_ENGINES,
                new LegoThreadFactory(ConstantUtil.PROVIDER_BUS));
        this.agents = new LinkedList<>();
        this.units = new LinkedList<>();
        this.active = new AtomicBoolean(false);

        this.commandQueue = new LinkedBlockingQueue<>();

        FrontHandUnit frontHandUnit = (FrontHandUnit)unitCache.get("frontHandUnit");
        frontHandUnit.setExecutor(executorForAgents);
        frontHandUnit.init(null, engineCache, sensorCache);
        this.units.add(frontHandUnit);
        this.agents.addAll(frontHandUnit.getAgents());


        PlatformUnit platformUnit = (PlatformUnit)unitCache.get("platformUnit");
        platformUnit.setExecutor(executorForAgents);
        platformUnit.init(null, engineCache, null);
        this.units.add(platformUnit);
        this.agents.addAll(platformUnit.getAgents());

        if(!agents.isEmpty()){
            active.set(true);
        }
    }

    @SuppressWarnings(value = "unchecked")
    @Override
    public boolean process(final GenericCommand<RequestCommandEnum> command) {
        switch (command.getType().getTarget()){
            case SYSTEM:
                return processSystemCommand(command);
            case PLATFORM:
                return processPlatformCommand(command);
            case HAND_UNIT:
                return processHandUnitCommand(command);
            default:
                throw new ClientException("no such command target= " + command );
        }
    }

    //Protected Methods
    @Override
    protected GenericAgent createAgent(String name, AgentProducer producer, AgentConsumer consumer) {
        return Objects.nonNull(producer) && Objects.nonNull(consumer) ? ProcessAgentBuilder.Builder(executorForAgents)
                .setName(name)
                .setProducer(producer)
                .setConsumer(consumer)
                .build() : null;
    }

    @Override
    protected Map<RoboUnitCommand, Function<ProcessAgent, AgentStatus>> initLogic(){
        return null;
    }


    //Private Methods
    /* currently system commad is executed as EXIT */
    private boolean processSystemCommand(final GenericCommand<RequestCommandEnum> command){
        switch (command.getType()){
            case EXIT:
                System.out.println("EXIT COMMAND HAS BEEN CALLED");
                active.set(false);
                executorForAgents.shutdown();
                return true;
            default:
                throw new ClientException("SYSTEM COMMAND= " + command );
        }
    }

    @SuppressWarnings(value = "unchecked")
    private boolean processPlatformCommand(final GenericCommand<RequestCommandEnum> command){
        PlatformUnit platformUnit = (PlatformUnit)units.get(AGENT_PLATFORM_POSITION);
        return platformUnit.process(command);
    }

    @SuppressWarnings(value = "unchecked")
    private boolean processHandUnitCommand(final GenericCommand<RequestCommandEnum> command){
        FrontHandUnit frontHandUnit = (FrontHandUnit)units.get(AGENT_HAND_POSITION);
        return frontHandUnit.process(command);
    }

}
