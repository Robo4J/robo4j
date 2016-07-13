/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This ClientFrontHandEngineConsumer.java is part of robo4j.
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

package com.robo4j.brick.fronthand;

import com.robo4j.commons.agent.AgentConsumer;
import com.robo4j.commons.concurrent.CoreBusQueue;
import lejos.robotics.RegulatedMotor;

import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;

/**
 * @author Miro Kopecky (@miragemiko)
 * @since 03.07.2016
 */
public class ClientFrontHandEngineConsumer implements AgentConsumer, Callable<Boolean> {

    private static final int ROTATION = 500;
    private Exchanger<Boolean> exchanger;
    private RegulatedMotor motorHandPortA;

    public ClientFrontHandEngineConsumer(Exchanger<Boolean> exchanger, RegulatedMotor engine){
        this.exchanger = exchanger;
        this.motorHandPortA = engine;
    }

    @Override
    public void setMessageQueue(CoreBusQueue commandsQueue) {
        throw new ClientFrontHandException("Not implemented messageQueue");
    }

    public RegulatedMotor getMotorHandPortA(){
        return motorHandPortA;
    }

    @Override
    public Boolean call() throws Exception {
        /* represent close  */
        boolean initValue = false;
        System.out.println("ENGINE STARTS");
        try {
            boolean state = exchanger.exchange(initValue);
            if(state) {
                /* is pushed */
                System.out.println("ENGINE  OFF = "+ Thread.currentThread().getName());
                motorHandPortA.rotate(ROTATION);
            } else {
                System.out.println("ENGINE ON = "+ Thread.currentThread().getName());
                motorHandPortA.rotate(-ROTATION);
            }
            System.out.println("SEND SIGNAL");
        } catch (InterruptedException e) {
            throw new ClientFrontHandException("FRONT_HAND= ",e);
        } finally {
            System.out.println("ENGINE DONE = "+ Thread.currentThread().getName());

        }
        /* engine is not active */
        return false;

    }

}

