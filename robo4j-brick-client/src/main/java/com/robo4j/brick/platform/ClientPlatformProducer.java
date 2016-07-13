/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This ClientPlatformProducer.java is part of robo4j.
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

package com.robo4j.brick.platform;

import com.robo4j.brick.client.enums.RequestCommandEnum;
import com.robo4j.commons.agent.AgentProducer;
import com.robo4j.commons.command.GenericCommand;
import com.robo4j.commons.concurrent.CoreBusQueue;

import java.util.concurrent.Exchanger;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Producer is responsible for taking line and
 * share it with consumer;
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 05.07.2016
 */
public class ClientPlatformProducer implements AgentProducer, Runnable {

    private LinkedBlockingQueue<GenericCommand<RequestCommandEnum>> commandQueue;
    private Exchanger<GenericCommand<RequestCommandEnum>> exchanger;
    public ClientPlatformProducer(final LinkedBlockingQueue<GenericCommand<RequestCommandEnum>> commandQueue,
                                  final Exchanger<GenericCommand<RequestCommandEnum>> exchanger) {
        this.commandQueue = commandQueue;
        this.exchanger = exchanger;
    }

    @Override
    public CoreBusQueue getMessageQueue() {
        return null;
    }

    @Override
    public void run() {

        GenericCommand<RequestCommandEnum> command = null;
        try {
            command= commandQueue.take();
            System.out.println("ClientPlatformProducer command = " + command);
            exchanger.exchange(command);
        } catch (InterruptedException e) {
            throw new ClientPlatformException("TOUCH PRODUCER e", e);
        } finally {
            System.out.println("ClientPlatformProducer exchanged= " + command);
        }
    }
}
