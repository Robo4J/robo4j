/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This FrontHandEngineConsumer.java is part of robo4j.
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

package com.robo4j.core.fronthand;

import com.robo4j.commons.agent.AgentConsumer;
import com.robo4j.commons.concurrent.CoreBusQueue;
import lejos.remote.ev3.RMIRegulatedMotor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;

/**
 * @author Miro Kopecky (@miragemiko)
 * @since 27.04.2016
 */
public class FrontHandEngineConsumer implements AgentConsumer, Callable<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(FrontHandEngineConsumer.class);

    private static final int ROTATION = 500;
    private Exchanger<Boolean> exchanger;
    private RMIRegulatedMotor motorHandPortA;

    public FrontHandEngineConsumer(Exchanger<Boolean> exchanger, RMIRegulatedMotor engine){
        this.exchanger = exchanger;
        this.motorHandPortA = engine;
    }

    @Override
    public void setMessageQueue(CoreBusQueue commandsQueue) {

    }

    public RMIRegulatedMotor getMotorHandPortA(){
        return motorHandPortA;
    }

    @Override
    public Boolean call() throws Exception {
        /* represent close  */
        boolean initValue = false;
        logger.info("ENGINE STARTS");
        try {
            boolean state = exchanger.exchange(initValue);
            if(state) {
                /* is pushed */
                logger.info("ENGINE  OFF = "+ Thread.currentThread().getName());
                motorHandPortA.rotate(ROTATION);
            } else {
                logger.info("ENGINE ON = "+ Thread.currentThread().getName());
                motorHandPortA.rotate(-ROTATION);
            }
            logger.info("SEND SIGNAL");
        } catch (InterruptedException | RemoteException e) {
            throw new FrontHandException("ENGINE CONSUMER e", e);
        } finally {
            logger.info("ENGINE DONE = "+ Thread.currentThread().getName());

        }
        /* engine is not active */
        return false;

    }
}
