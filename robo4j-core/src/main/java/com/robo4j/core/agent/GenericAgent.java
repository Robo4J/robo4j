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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * Created by miroslavkopecky on 29/05/16.
 */
public class GenericAgent implements RoboAgent {

    private static final Logger logger = LoggerFactory.getLogger(GenericAgent.class);
    private final AgentCache cache;
    private final ExecutorService executor;
    private final AgentProducer producer;
    private final AgentConsumer consumer;

    @SuppressWarnings(value = "unchecked")
    public GenericAgent(final ExecutorService executor, final AgentProducer producer, final AgentConsumer consumer) {
        cache = new AgentCache();
        this.executor = executor;
        this.producer = producer;
        this.consumer = consumer;
        consumer.setCommandsQueue(producer.getCommandsQueue());
    }


    public AgentStatus activate(){

        executor.execute((Runnable) producer);
        executor.execute((Runnable) consumer);

        final AgentStatus result = new AgentStatus(AgentStatusEnum.ACTIVE);
        cache.put(result.toString());
        logger.info("AGENT ACTIVE = " + cache);
        return result;
    }

}
