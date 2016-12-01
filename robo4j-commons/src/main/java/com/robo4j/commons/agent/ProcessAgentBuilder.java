/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This ProcessAgentBuilder.java  is part of robo4j.
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

import java.util.concurrent.ExecutorService;

/**
 * Builder
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 30.06.2016
 */
public class ProcessAgentBuilder<AgentType extends DefaultAgent>  {

    private final AgentType agent;
    private final ExecutorService executor;
    private String name;
    private AgentProducer producer;
    private AgentConsumer consumer;

    @SuppressWarnings(value = "unchecked")
    private ProcessAgentBuilder(ExecutorService executor){
        try {
            Class<ProcessAgent> clazz = ProcessAgent.class;
            this.agent = (AgentType) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Missing DefaultAgent type class");
        }
        this.executor = executor;
    }

    public ProcessAgentBuilder<AgentType> setName(String name) {
        this.name = name;
        return this;
    }

    public ProcessAgentBuilder<AgentType> setProducer(AgentProducer producer) {
        this.producer = producer;
        return this;
    }

    public ProcessAgentBuilder<AgentType> setConsumer(AgentConsumer consumer) {
        this.consumer = consumer;
        return this;
    }

    @SuppressWarnings(value = "unchecked")
    public ProcessAgent build(){
        agent.setExecutor(executor);
        agent.setName(name);
        agent.setProducer(producer);
        agent.setConsumer(consumer);
        return (ProcessAgent)agent;
    }

    public static ProcessAgentBuilder<DefaultAgent> Builder(ExecutorService executor){
        return new ProcessAgentBuilder<>(executor);
    }

}
