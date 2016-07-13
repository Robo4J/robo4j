/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This PlatformProducer.java is part of robo4j.
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

package com.robo4j.core.platform;

import com.robo4j.commons.agent.AgentProducer;
import com.robo4j.commons.command.GenericCommand;
import com.robo4j.commons.concurrent.CoreBusQueue;
import com.robo4j.core.platform.command.LegoPlatformCommandEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Exchanger;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Platform Producer is responsible for taking command and forwarding them to consumer
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 09.07.2016
 */
public class PlatformProducer implements AgentProducer, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PlatformProducer.class);
    private LinkedBlockingQueue<GenericCommand<LegoPlatformCommandEnum>> commandQueue;
    private Exchanger<GenericCommand<LegoPlatformCommandEnum>> exchanger;

    public PlatformProducer(final LinkedBlockingQueue<GenericCommand<LegoPlatformCommandEnum>> commandQueue,
                            final Exchanger<GenericCommand<LegoPlatformCommandEnum>> exchanger) {
        this.commandQueue = commandQueue;
        this.exchanger = exchanger;
    }

    @Override
    public CoreBusQueue getMessageQueue() {
        return null;
    }

    @Override
    public void run() {
        GenericCommand<LegoPlatformCommandEnum> command = null;
        try {
            command= commandQueue.take();
            logger.info("PlatformProducer command = " + command);
            exchanger.exchange(command);
        } catch (InterruptedException e) {
            throw new PlatformException("Platform Engines e", e);
        } finally {
            logger.info("PlatformProducer exchanged= " + command);
        }
    }

}
