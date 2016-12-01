/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This BrickMainAgent.java  is part of robo4j.
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

package com.robo4j.brick.client.agent;

import com.robo4j.commons.agent.AgentCache;
import com.robo4j.commons.agent.AgentConsumer;
import com.robo4j.commons.agent.AgentProducer;
import com.robo4j.commons.agent.AgentStatus;
import com.robo4j.commons.agent.AgentStatusEnum;
import com.robo4j.commons.agent.GenericAgent;
import com.robo4j.commons.agent.ReceiverAgent;

import java.util.concurrent.ExecutorService;

/**
 * MainBrick agent responsible for handling incoming command and moving them to
 * the desired destination (unit, agent)
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 10.06.2016
 */
public class BrickMainAgent implements ReceiverAgent {

    private final AgentCache<AgentStatus> cache;
    private final ExecutorService executor;
    private final AgentProducer producer;
    private final AgentConsumer consumer;

    @SuppressWarnings(value = "unchecked")
    public BrickMainAgent(ExecutorService executor, AgentProducer producer, AgentConsumer consumer) {
        this.cache = new AgentCache();
        this.executor = executor;
        this.producer = producer;
        this.consumer = consumer;
        consumer.setMessageQueue(producer.getMessageQueue());
    }

    @Override
    public AgentStatus activate(){

        executor.execute((Runnable) producer);
        executor.execute((Runnable) consumer);

        final AgentStatus result = new AgentStatus<String>(AgentStatusEnum.ACTIVE);
        cache.put(result);
        return result;
    }

    @Override
    public AgentCache<AgentStatus> getCache() {
        return cache;
    }

    @Override
    public void addStatus(AgentStatus status){
        this.cache.put(status);
    }
}
