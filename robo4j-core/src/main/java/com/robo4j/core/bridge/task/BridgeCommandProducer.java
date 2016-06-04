/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This BridgeCommandProducer.java is part of robo4j.
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


import com.robo4j.core.agent.AgentProducer;
import com.robo4j.core.bridge.BridgeBusQueue;
import com.robo4j.core.bridge.BridgeUtils;
import com.robo4j.core.bridge.command.BridgeCommand;
import com.robo4j.core.fronthand.command.FrontHandCommandEnum;
import com.robo4j.core.platform.PlatformProperties;
import com.robo4j.core.platform.command.LegoPlatformCommandEnum;
import com.robo4j.core.system.QueueFIFOEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;

/**
 * Created by miroslavkopecky on 03/04/16.
 */


public class BridgeCommandProducer implements AgentProducer, Runnable{

    private static final Logger logger = LoggerFactory.getLogger(BridgeCommandProducer.class);
    private static final int AWAIT_SECONDS = 2;
    private static final int DEFAULT_PRIORITY = 1;
    private static final String COMMAND_DELIMITER = ",";
    private static final int COMMAND_NAME_POSS = 1;
    private static final int COMMAND_VALUE_POSS= 2;
    private BridgeBusQueue<QueueFIFOEntry<BridgeCommand>> commandsQueue;
    private LinkedBlockingQueue<String> commandLineQueue;
    private volatile AtomicBoolean active;
    private volatile AtomicBoolean emergency;
    private PlatformProperties properties;

    public BridgeCommandProducer(AtomicBoolean emergency, AtomicBoolean active, LinkedBlockingQueue<String> commandLineQueue, PlatformProperties properties) {
        this.emergency = emergency;
        this.active = active;
        this.commandsQueue = new BridgeBusQueue<>(AWAIT_SECONDS);    //BridgeCommand
        this.commandLineQueue = commandLineQueue;
        this.properties = properties;
        logger.info("BridgeCommandProducer INIT");
    }

    @Override
    public BridgeBusQueue<QueueFIFOEntry<BridgeCommand>> getCommandsQueue(){
        return commandsQueue;
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public void run() {
        try{
            while(active.get()){
                logger.debug("BridgeCommandProducer READY and IN CYCLE");
                if(emergency.get()){
                    logger.info("MERGENCY COMMAND ACTIVE ");
                }
                final String commandLine = commandLineQueue.take();
                final String[] separateCommandLines = commandLine.split(COMMAND_DELIMITER);

                for(String command: separateCommandLines){
                    if(command.equals(FrontHandCommandEnum.COMMAND.getName())){
                        BridgeCommand bridgeCommand = new BridgeCommand(properties,
                                null, FrontHandCommandEnum.COMMAND.getName(), DEFAULT_PRIORITY);
                        commandsQueue.transfer(new QueueFIFOEntry(bridgeCommand));
                    } else {
                        Matcher matcherMain = BridgeUtils.commandLinePattern.matcher(command.trim());
                        if(matcherMain.find()){
                            BridgeCommand bridgeCommand = new BridgeCommand(properties,
                                    LegoPlatformCommandEnum.getCommand(matcherMain.group(COMMAND_NAME_POSS)),
                                    matcherMain.group(COMMAND_VALUE_POSS), DEFAULT_PRIORITY);
                            logger.debug("COMMAND TO TRANSFER = " + bridgeCommand);
                            commandsQueue.transfer(new QueueFIFOEntry(bridgeCommand));
                        }
                    }



                }
            }
            logger.debug("BRIDGE COMMAND PRODUCER DONE");
        } catch (InterruptedException e){
            logger.debug("EXCEPTION = " + e);
        }
    }
}
