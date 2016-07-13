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

package com.robo4j.line.unit;

import com.robo4j.commons.agent.AgentConsumer;
import com.robo4j.commons.agent.AgentProducer;
import com.robo4j.commons.agent.AgentStatus;
import com.robo4j.commons.agent.AgentStatusEnum;
import com.robo4j.commons.agent.GenericAgent;
import com.robo4j.commons.agent.ProcessAgent;
import com.robo4j.commons.agent.ProcessAgentBuilder;
import com.robo4j.commons.annotation.RoboUnit;
import com.robo4j.commons.command.FrontHandCommandEnum;
import com.robo4j.commons.command.RoboUnitCommand;
import com.robo4j.commons.concurrent.LegoThreadFactory;
import com.robo4j.commons.unit.DefaultUnit;
import com.robo4j.core.fronthand.FrontHandEngineConsumer;
import com.robo4j.core.fronthand.FrontHandException;
import com.robo4j.core.fronthand.FrontHandTouchProducer;
import com.robo4j.core.fronthand.FrontHandUtils;
import com.robo4j.core.system.dto.LegoEngineDTO;
import com.robo4j.lego.control.LegoBrickRemote;
import com.robo4j.lego.control.LegoEngine;
import com.robo4j.lego.control.LegoSensor;
import com.robo4j.lego.control.LegoUnit;
import com.robo4j.lego.enums.LegoAnalogPortEnum;
import com.robo4j.lego.enums.LegoEngineEnum;
import com.robo4j.lego.enums.LegoEnginePartEnum;
import lejos.remote.ev3.RMIRegulatedMotor;

import java.rmi.RemoteException;
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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static com.robo4j.core.lego.rmi.LegoUnitProviderUtil.createRMIEngine;
import static com.robo4j.core.lego.rmi.LegoUnitProviderUtil.createTouchSensor;

/**
 * @author Miro Kopecky (@miragemiko)
 * @since 03.07.2016
 */

@RoboUnit(value = FrontHandUnit.UNIT_NAME,
        system = FrontHandUnit.SYSTEM_NAME,
        producer= FrontHandUnit.PRODUCER_NAME,
        consumer= FrontHandUnit.CONSUMER_NAME)
public class FrontHandUnit extends DefaultUnit implements LegoUnit {

    /* all connected pars  */
    private static final int CONNECTED_ELEMENTS = 2;
    private static final int DEFAULT_SPEED = 300;
    private static final int AGENT_POSITION = 0;
    static final String UNIT_NAME = "frontHandUnit";
    static final String SYSTEM_NAME = "legoBrick1";
    static final String PRODUCER_NAME = "frontHandSensor";
    static final String CONSUMER_NAME = "frontHand";

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

            this.executorForAgents = Executors.newFixedThreadPool(CONNECTED_ELEMENTS,
                    new LegoThreadFactory(FrontHandUtils.BUS_FRONT_HAND));
            this.agents = new ArrayList<>();

            this.active = new AtomicBoolean(false);

            Exchanger<Boolean> exchanger = new Exchanger<>();
            this.agents.add(createAgent(
                    "frontHandAgent",
                    new FrontHandTouchProducer(exchanger, sensorCache.entrySet().stream()
                            .filter(sensorEntry -> sensorEntry.getValue().getPart().equals(LegoEnginePartEnum.HAND))
                            .map(Map.Entry::getValue)
                            .map(legoSensor -> createTouchSensor(legoBrickRemote, legoSensor.getPort()))
                            .reduce(null, (e1, e2) -> e2)),

                    new FrontHandEngineConsumer(exchanger,  engineCache.entrySet().stream()
                            .filter(entry -> entry.getValue().getPart().equals(LegoEnginePartEnum.HAND))
                            .map(Map.Entry::getValue)
                            .map(legoEngine -> createEngine(legoBrickRemote, legoEngine.getPort(), DEFAULT_SPEED))
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
            return false;
        }

        ProcessAgent processAgent = (ProcessAgent) agents.get(AGENT_POSITION);
        return processAgent.process(command, (cm) -> {
            final FrontHandCommandEnum commandEnum = (FrontHandCommandEnum)cm;
            return logic.get(commandEnum).apply(processAgent);
        }).getStatus().equals(AgentStatusEnum.ACTIVE);

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
                .setName(name)
                .setProducer(producer)
                .setConsumer(consumer)
                .build() : null;
    }

    @SuppressWarnings(value = "unchecked")
    @Override
    protected Map<RoboUnitCommand, Function<ProcessAgent, AgentStatus>> initLogic(){
        final Map<RoboUnitCommand, Function<ProcessAgent, AgentStatus>> result = new HashMap<>();
        result.put(FrontHandCommandEnum.COMMAND, (ProcessAgent agent) -> {
            agent.setActive(true);
            agent.getExecutor().execute((Runnable) agent.getProducer());
            final Future<Boolean> engineActive = agent.getExecutor().submit((Callable<Boolean>) agent.getConsumer());
            try {
                agent.setActive(engineActive.get());
            } catch (InterruptedException | ConcurrentModificationException | ExecutionException e) {
                throw new FrontHandException("SOMETHING ERROR CYCLE COMMAND= ", e);
            }
            return new AgentStatus<String>(AgentStatusEnum.ACTIVE);
        });

        result.put(FrontHandCommandEnum.EXIT, (ProcessAgent agent) -> {
            FrontHandEngineConsumer consumer = (FrontHandEngineConsumer) agent.getConsumer();
            FrontHandTouchProducer producer = (FrontHandTouchProducer) agent.getProducer();
            try {
                consumer.getMotorHandPortA().close();
                producer.getTouchSensor().close();
            } catch (RemoteException e) {
                throw new FrontHandException("RUN ERROR PROCESS: ", e);
            }
            agent.getExecutor().shutdown();
            agent.setActive(false);
            active.set(false);
            return new AgentStatus<String>(AgentStatusEnum.OFFLINE);
        });
        return Collections.unmodifiableMap(result);
    }

    //Private Methods
    private RMIRegulatedMotor createEngine(final LegoBrickRemote legoBrickRemote, final LegoAnalogPortEnum type, int speed ){
        try {
            return createRMIEngine(legoBrickRemote, new LegoEngineDTO(type, LegoEngineEnum.MEDIUM), speed);
        } catch (RemoteException e) {
            throw new FrontHandException("FRONT HAND ENGINE INIT FAIL: ", e);
        }
    }


}
