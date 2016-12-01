/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This CommandProcessor.java  is part of robo4j.
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

package com.robo4j.brick.client.command;

import com.robo4j.brick.bus.ClientBusQueue;
import com.robo4j.brick.client.enums.RequestCommandEnum;
import com.robo4j.brick.dto.ClientCommandRequestDTO;
import com.robo4j.brick.logging.SimpleLoggingUtil;
import com.robo4j.brick.util.ConstantUtil;
import com.robo4j.commons.agent.AgentProducer;
import com.robo4j.commons.command.GenericCommand;
import com.robo4j.commons.concurrent.QueueFIFOEntry;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * Command Processor is singleton
 * Command Processor is producer
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 09.06.2016
 */
public final class CommandProcessor implements AgentProducer, Runnable{

    private static final int AWAIT_SECONDS = 2;
    private volatile AtomicBoolean active;
    private volatile ClientBusQueue messageQueue;
    private volatile LinkedBlockingQueue<List<ClientCommandRequestDTO>> inputQueue;

    public CommandProcessor(AtomicBoolean active, LinkedBlockingQueue<List<ClientCommandRequestDTO>> inputQueue){
        messageQueue = new ClientBusQueue<QueueFIFOEntry<?>>(AWAIT_SECONDS);
        this.active = active;
        this.inputQueue = inputQueue;
        SimpleLoggingUtil.print(getClass(),"PRODUCER UP");
    }

    @Override
    public ClientBusQueue getMessageQueue() {
        return messageQueue;
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public void run() {
        //TODO: improve this part separate
        try {
            while(active.get()) {
                final List<ClientCommandRequestDTO> commandQueue = inputQueue.take();
                for (ClientCommandRequestDTO element : commandQueue) {
                    switch (element.getType()) {
                        case EXIT:
                        case MOVE:
                        case BACK:
                        case LEFT:
                        case RIGHT:
                        case STOP:
                        case HAND:
                        case FRONT_LEFT:
                        case FRONT_RIGHT:
                            messageQueue.transfer(getCommand(element.getType(), element.getValue(), element.getSpeed()));
                            break;
                        default:
                    }

                }

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    //Private Methods
    @SuppressWarnings(value = "unchecked")
    private QueueFIFOEntry getCommand(RequestCommandEnum type, String value, String speed){
        /* client command holding default values */
        final ClientCommandProperties properties = new ClientCommandProperties(Integer.parseInt(speed));
        final GenericCommand<RequestCommandEnum> command = new GenericCommand<>(properties, type, value, ConstantUtil.DEFAULT_PRIORITY);
        return new QueueFIFOEntry<>(command);
    }
}
