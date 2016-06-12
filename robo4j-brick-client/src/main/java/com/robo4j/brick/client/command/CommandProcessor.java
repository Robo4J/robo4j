/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This CommandProcessor.java is part of robo4j.
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

package com.robo4j.brick.client.command;

import com.robo4j.brick.bus.ClientBusQueue;
import com.robo4j.brick.client.enums.RequestCommandEnum;
import com.robo4j.brick.client.io.ClientException;
import com.robo4j.brick.client.util.ClientCommException;
import com.robo4j.brick.dto.ClientRequestDTO;
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
 * Created by miroslavkopecky on 09/06/16.
 */
public final class CommandProcessor implements AgentProducer, Runnable{

    private static final int AWAIT_SECONDS = 2;
    private volatile AtomicBoolean active;
    private volatile ClientBusQueue messageQueue;
    private LinkedBlockingQueue<List<ClientRequestDTO>> inputQueue;

    public CommandProcessor(AtomicBoolean active, LinkedBlockingQueue<List<ClientRequestDTO>> inputQueue){
        messageQueue = new ClientBusQueue<QueueFIFOEntry<?>>(AWAIT_SECONDS);
        this.active = active;
        this.inputQueue = inputQueue;
    }

    @Override
    public ClientBusQueue getMessageQueue() {
        return messageQueue;
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public void run() {
        try {
            while(active.get()) {
                final List<ClientRequestDTO> commandQueue = inputQueue.take();

                for (ClientRequestDTO element : commandQueue) {
                    switch (element.getType()) {
                        case EXIT:
                        case MOVE:
                        case BACK:
                        case LEFT:
                        case RIGHT:
                            messageQueue.transfer(getCommand(element.getType(), element.getValue()));
                            break;
                        default:
                            throw new ClientCommException("NO such command element= " + element);
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new ClientException("Command Processor problem ", e);
        }

    }

    //Private Methods
    @SuppressWarnings(value = "unchecked")
    private QueueFIFOEntry getCommand(RequestCommandEnum type, String value){
        /* client command holding default values */
        final ClientCommandProperties properties = new ClientCommandProperties();
        final GenericCommand<RequestCommandEnum> command = new GenericCommand<>(properties, type, value, ConstantUtil.DEFAULT_PRIORITY);
        return new QueueFIFOEntry<>(command);
    }
}
