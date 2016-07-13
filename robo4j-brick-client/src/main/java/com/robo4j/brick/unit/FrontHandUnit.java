/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This FrontHandUnit.java is part of robo4j.
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

package com.robo4j.brick.unit;

import com.robo4j.brick.client.enums.RequestCommandEnum;
import com.robo4j.brick.fronthand.ClientFrontHandEngineConsumer;
import com.robo4j.brick.fronthand.ClientFrontHandException;
import com.robo4j.brick.fronthand.ClientFrontHandTouchProducer;
import com.robo4j.brick.util.LegoClientUnitProviderUtil;
import com.robo4j.commons.agent.AgentConsumer;
import com.robo4j.commons.agent.AgentProducer;
import com.robo4j.commons.agent.AgentStatus;
import com.robo4j.commons.agent.AgentStatusEnum;
import com.robo4j.commons.agent.GenericAgent;
import com.robo4j.commons.agent.ProcessAgent;
import com.robo4j.commons.agent.ProcessAgentBuilder;
import com.robo4j.commons.annotation.RoboUnit;
import com.robo4j.commons.command.FrontHandCommandEnum;
import com.robo4j.commons.command.GenericCommand;
import com.robo4j.commons.command.RoboUnitCommand;
import com.robo4j.commons.unit.DefaultUnit;
import com.robo4j.lego.control.LegoBrickRemote;
import com.robo4j.lego.control.LegoEngine;
import com.robo4j.lego.control.LegoSensor;
import com.robo4j.lego.control.LegoUnit;
import com.robo4j.lego.enums.LegoEnginePartEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;


/**
 * @author Miro Kopecky (@miragemiko)
 * @since 03.07.2016
 */

@RoboUnit(value = FrontHandUnit.UNIT_NAME,
        system = FrontHandUnit.SYSTEM_NAME,
        producer = FrontHandUnit.PRODUCER_NAME,
        consumer = FrontHandUnit.CONSUMER_NAME)
public class FrontHandUnit extends DefaultUnit implements LegoUnit {

    /* all connected pars  */
    private static final int AGENT_HAND_POSITION = 0;
    static final String UNIT_NAME = "frontHandUnit";
    static final String SYSTEM_NAME = "legoBrick1";
    static final String PRODUCER_NAME = "frontHandSensor";
    static final String CONSUMER_NAME = "frontHandEngine";

    public FrontHandUnit() {
    }

    @Override
    public void setExecutor(final ExecutorService executor){
        this.executorForAgents = executor;
    }

    @Override
    public LegoUnit init(final LegoBrickRemote legoBrickRemote,
                         final Map<String, LegoEngine> engineCache,
                         final Map<String, LegoSensor> sensorCache){

        if(Objects.nonNull(executorForAgents)){

            this.agents = new ArrayList<>();

            this.active = new AtomicBoolean(false);

            final Exchanger<Boolean> frontHandExchanger = new Exchanger<>();
            this.agents.add(createAgent("frontHandAgent",
                    new ClientFrontHandTouchProducer(frontHandExchanger, sensorCache.entrySet().stream()
                            .filter(sensorEntry -> sensorEntry.getValue().getPart().equals(LegoEnginePartEnum.HAND))
                            .map(Map.Entry::getValue)
                            .map(LegoClientUnitProviderUtil::createTouchSensor)
                            .reduce(null, (e1, e2) -> e2)),

                    new ClientFrontHandEngineConsumer(frontHandExchanger,  engineCache.entrySet().stream()
                            .filter(entry -> entry.getValue().getPart().equals(LegoEnginePartEnum.HAND))
                            .map(Map.Entry::getValue)
                            .map(LegoClientUnitProviderUtil::createEngine)
                            .reduce(null, (e1, e2) -> e2))));

            if(!agents.isEmpty()){
                active.set(true);
                logic = initLogic();
            }
        }

        return this;

    }

    @SuppressWarnings(value = "unchecked")
    @Override
    public boolean process(RoboUnitCommand command){
        if(!active.get()){
            System.out.println("FrontHand NOT PROCESSED");
            return false;
        } else {
            final ProcessAgent processAgent = (ProcessAgent) agents.get(AGENT_HAND_POSITION);
            final GenericCommand<RequestCommandEnum> genericCommand = (GenericCommand)command;
            final RoboUnitCommand roboUnitCommand = FrontHandCommandEnum.getCommand(genericCommand.getValue());
            return processAgent.process(roboUnitCommand, (cm) -> {
                final FrontHandCommandEnum commandEnum = (FrontHandCommandEnum)cm;
                return logic.get(commandEnum).apply(processAgent);
            }).getStatus().equals(AgentStatusEnum.ACTIVE);
        }

    }

    @Override
    public boolean isActive() {
        return active.get();
    }

    @Override
    public String getUnitName() {
        return UNIT_NAME;
    }

    @Override
    public String getSystemName() {
        return SYSTEM_NAME;
    }

    @Override
    public String getProducerName() {
        return PRODUCER_NAME;
    }

    @Override
    public String getConsumerName() {
        return CONSUMER_NAME;
    }

    //Protected Methods
    @Override
    protected GenericAgent createAgent(String name, AgentProducer producer, AgentConsumer consumer) {
        return Objects.nonNull(producer) && Objects.nonNull(consumer) ? ProcessAgentBuilder.Builder(executorForAgents)
                .setProducer(producer)
                .setConsumer(consumer)
                .build() : null;
    }

    @SuppressWarnings(value = "unchecked")
    @Override
    protected Map<RoboUnitCommand, Function<ProcessAgent, AgentStatus>> initLogic(){
        final Map<RoboUnitCommand, Function<ProcessAgent, AgentStatus>> result = new HashMap<>();
        System.out.println("RoboUnit Logic");
        result.put(FrontHandCommandEnum.COMMAND, (ProcessAgent agent) -> {
            agent.setActive(true);
            agent.getExecutor().execute((Runnable) agent.getProducer());
            final Future<Boolean> engineActive = agent.getExecutor().submit((Callable<Boolean>) agent.getConsumer());
            try {
                System.out.println("RoboUnit FrontHand COMMAND");
                agent.setActive(engineActive.get());
            } catch (InterruptedException | ConcurrentModificationException | ExecutionException e) {
                throw new ClientFrontHandException("SOMETHING ERROR CYCLE COMMAND= ", e);
            }
            return new AgentStatus<String>(AgentStatusEnum.ACTIVE);
        });

        result.put(FrontHandCommandEnum.EXIT, (ProcessAgent agent) -> {
            System.out.println("RoboUnit FrontHand EXIT");
            ClientFrontHandEngineConsumer consumer = (ClientFrontHandEngineConsumer) agent.getConsumer();
            ClientFrontHandTouchProducer producer = (ClientFrontHandTouchProducer) agent.getProducer();
            consumer.getMotorHandPortA().close();
            producer.getTouchSensor().close();
            agent.getExecutor().shutdown();
            agent.setActive(false);
            active.set(false);
            return new AgentStatus<String>(AgentStatusEnum.OFFLINE);
        });

        return Collections.unmodifiableMap(result);
    }

}
