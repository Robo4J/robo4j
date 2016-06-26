/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This GenericAgentBuilder.java is part of robo4j.
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

import com.robo4j.commons.agent.AgentConsumer;
import com.robo4j.commons.agent.AgentProducer;
import com.robo4j.commons.agent.DefaultAgent;

import java.util.concurrent.ExecutorService;

/**
 * Generic agent builder
 * initial version
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 22.06.2016
 */
public class GenericAgentBuilder<AgentType extends DefaultAgent> {

    private static GenericAgentBuilder<DefaultAgent> target;
    private final AgentType agent;
    private final ExecutorService executor;
    private AgentProducer producer;
    private AgentConsumer consumer;

    @SuppressWarnings(value = "unchecked")
    private GenericAgentBuilder(ExecutorService executor){
        try {
            Class<GenericAgent> clazz = GenericAgent.class;
            this.agent = (AgentType) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Missing DefaultAgent type class");
        }
        this.executor = executor;
    }

    public GenericAgentBuilder<AgentType> setProducer(AgentProducer producer) {
        this.producer = producer;
        return this;
    }

    public GenericAgentBuilder<AgentType> setConsumer(AgentConsumer consumer) {
        this.consumer = consumer;
        return this;
    }

    @SuppressWarnings(value = "unchecked")
    public GenericAgent build(){
        agent.setExecutor(executor);
        agent.setProducer(producer);
        consumer.setMessageQueue(producer.getMessageQueue());
        agent.setConsumer(consumer);
        return (GenericAgent)agent;
    }

    public static GenericAgentBuilder<DefaultAgent> Builder(ExecutorService executor){
        target = new GenericAgentBuilder<>(executor);
        return target;
    }


}
