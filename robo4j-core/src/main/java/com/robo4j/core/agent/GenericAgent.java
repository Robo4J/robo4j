/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This GenericAgent.java is part of robo4j.
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

package com.robo4j.core.agent;

import com.robo4j.commons.agent.AgentCache;
import com.robo4j.commons.agent.AgentConsumer;
import com.robo4j.commons.agent.AgentProducer;
import com.robo4j.commons.agent.AgentStatus;
import com.robo4j.commons.agent.AgentStatusEnum;
import com.robo4j.commons.agent.DefaultAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

/**
 * Current RoboCache is implement with String
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 29.05.2016
 */
public class GenericAgent implements DefaultAgent {

    private static final Logger logger = LoggerFactory.getLogger(GenericAgent.class);
    private final AgentCache<AgentStatus> cache;
    private ExecutorService executor;
    private String name;
    private AgentProducer producer;
    private AgentConsumer consumer;


    public GenericAgent(){
        cache = new AgentCache<>();
    }
    @SuppressWarnings(value = "unchecked")
    public GenericAgent(final ExecutorService executor, final AgentProducer producer, final AgentConsumer consumer) {
        cache = new AgentCache();
        this.executor = executor;
        this.producer = producer;
        this.consumer = consumer;
        consumer.setMessageQueue(producer.getMessageQueue());
    }

    public AgentStatus activate() {
        return activateFunction().apply(producer, consumer);
    }

    @Override
    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setProducer(AgentProducer producer) {
        this.producer = producer;
    }

    @Override
    public void setConsumer(AgentConsumer consumer) {
        this.consumer = consumer;
    }

    //Private Methods
    private BiFunction<AgentProducer, AgentConsumer, AgentStatus> activateFunction(){
        return (AgentProducer producer, AgentConsumer consumer) -> {
            executor.execute((Runnable) producer);
            executor.execute((Runnable) consumer);
            final AgentStatus result = new AgentStatus<String>(AgentStatusEnum.ACTIVE);
            cache.put(result);
            logger.info("AGENT ACTIVE = " + cache);
            return result;
        };
    }
}
