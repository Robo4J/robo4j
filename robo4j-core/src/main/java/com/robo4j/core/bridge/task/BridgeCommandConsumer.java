/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This BridgeCommandConsumer.java is part of robo4j.
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

package com.robo4j.core.bridge.task;

import com.robo4j.commons.agent.AgentConsumer;
import com.robo4j.commons.command.FrontHandCommandEnum;
import com.robo4j.commons.concurrent.CoreBusQueue;
import com.robo4j.commons.concurrent.LegoThreadFactory;
import com.robo4j.core.bridge.BridgeUtils;
import com.robo4j.core.bridge.command.BridgeCommand;
import com.robo4j.core.lego.LegoException;
import com.robo4j.core.platform.command.LegoCommandProperty;
import com.robo4j.core.platform.command.LegoPlatformCommandEnum;
import com.robo4j.core.platform.provider.LegoBrickCommandsProvider;
import com.robo4j.lego.control.LegoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * Bridge Command Consumer consumes event/tasks from Producer
 * and provides them to process
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 03.04.2016
 */
public class BridgeCommandConsumer<QueueType extends CoreBusQueue> implements AgentConsumer, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(BridgeCommandConsumer.class);
    private static volatile ExecutorService executorBridgeConsumer;
    private volatile AtomicBoolean active;
    private LegoBrickCommandsProvider legoBrickCommandsProvider;
    private LegoUnit legoFrontHandUnit;
    private QueueType commandsQueue; //


    public BridgeCommandConsumer(AtomicBoolean active,
                                 LegoBrickCommandsProvider legoBrickCommandsProvider,
                                 LegoUnit legoFrontHandUnit) {
        this.active = active;
        this.legoBrickCommandsProvider = legoBrickCommandsProvider;
        this.legoFrontHandUnit = legoFrontHandUnit;
        executorBridgeConsumer = Executors.newSingleThreadExecutor(new LegoThreadFactory(BridgeUtils.BUS_COMMAND_CONSUMER));
    }


    @SuppressWarnings(value = "unchecked")
    @Override
    public void setMessageQueue(CoreBusQueue commandsQueue) {
        this.commandsQueue = (QueueType)commandsQueue;
    }


    @Override
    public void run() {

        if(commandsQueue == null){
             throw new LegoException("ERROR: consumer queue");
        }

        while(active.get() && commandsQueue.peek() != null){
            try {
                final BridgeCommand bridgeCommand = (BridgeCommand) commandsQueue.take().getEntry();
                Future<Boolean> moveFuture = null;

                if(Objects.isNull(bridgeCommand.getType())){
                    moveFuture = executorBridgeConsumer.submit(() ->
                        legoFrontHandUnit.process(FrontHandCommandEnum.getCommand(bridgeCommand.getValue()))
                    );

                } else {
                    switch (bridgeCommand.getType()){
                        case MOVE:
                            moveFuture= processBridgeCommand(bridgeCommand, LegoPlatformCommandEnum.MOVE);
                            break;
                        case BACK:
                            moveFuture= processBridgeCommand(bridgeCommand, LegoPlatformCommandEnum.BACK);
                            break;
                        case LEFT:
                            moveFuture= processBridgeCommand(bridgeCommand, LegoPlatformCommandEnum.LEFT);
                            break;
                        case RIGHT:
                            moveFuture= processBridgeCommand(bridgeCommand, LegoPlatformCommandEnum.RIGHT);
                            break;
                        case CLOSE:
                        case EXIT:
                        case STOP:
                            break;
                        default:
                            throw new LegoException("NO SUCH COMMAND = " + bridgeCommand);
                    }

                }

                final boolean result = Objects.nonNull(moveFuture) ? moveFuture.get() : false;
                if(!result)  throw new LegoException(" NO RESULT  = " + bridgeCommand);

            } catch (InterruptedException | ExecutionException e) {
                logger.error("RUN ERROR = " + e);
            }
        }

        executorBridgeConsumer.shutdown();
        logger.info("COMMAND CONSUMER DONE");
    }

    //Private Method
    private Future<Boolean> processBridgeCommand(BridgeCommand bridgeCommand, LegoPlatformCommandEnum type){
        return executorBridgeConsumer.submit(() -> {
                logger.info("PROCESS BRIDGE COMMAND = " + bridgeCommand);
                logger.info("PROCESS BRIDGE COMMAND speed= " + bridgeCommand.getProperties().getCycles());
                logger.info("PROCESS BRIDGE COMMAND value= " + bridgeCommand.getValue());

                return legoBrickCommandsProvider.process(type,
                    new LegoCommandProperty(bridgeCommand.getValue(), bridgeCommand.getProperties().getCycles()));
            });
    }
}
