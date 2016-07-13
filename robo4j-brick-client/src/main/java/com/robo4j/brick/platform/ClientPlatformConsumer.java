/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This ClientPlatformConsumer.java is part of robo4j.
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
import com.robo4j.brick.client.io.ClientException;
import com.robo4j.brick.util.LegoClientUnitProviderUtil;
import com.robo4j.commons.agent.AgentConsumer;
import com.robo4j.commons.command.GenericCommand;
import com.robo4j.commons.concurrent.CoreBusQueue;
import com.robo4j.lego.control.LegoEngine;
import lejos.robotics.RegulatedMotor;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import static com.robo4j.lego.util.LegoPlatformUtil.adjustCyclesByValue;

/**
 * ClientPlatformConsumer is responsible for interaction with
 * Hardware resources
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 05.07.2016
 */
public class ClientPlatformConsumer implements AgentConsumer, Callable<Boolean> {

    private static final int CENTIMETER_CYCLES = 33;
    private static final int ROTATION_CYCLES = 9;
    private static final int DEFAULT_0 = 0;
    private static final int DEFAULT_1 = 1;
    private static final String RIGHT = "right";
    private static final String LEFT = "left";

    private ExecutorService executor;
    private Exchanger<GenericCommand<RequestCommandEnum>> exchanger;
    private volatile RegulatedMotor rightMotor;
    private volatile RegulatedMotor leftMotor;
    private ReentrantLock lock;

    public ClientPlatformConsumer(final ExecutorService executor,
                                  final Exchanger<GenericCommand<RequestCommandEnum>> exchanger,
                                  final Map<String, LegoEngine> engineCache ) {
        this.lock = new ReentrantLock();
        this.executor = executor;
        this.exchanger = exchanger;
        this.rightMotor = LegoClientUnitProviderUtil.createEngine(engineCache.get(RIGHT));
        this.leftMotor = LegoClientUnitProviderUtil.createEngine(engineCache.get(LEFT));
    }

    @Override
    public void setMessageQueue(CoreBusQueue commandsQueue) {
        throw new ClientPlatformException("NOT IMPLEMENTED messageQueue");
    }

    @Override
    public Boolean call() throws Exception {
        final GenericCommand<RequestCommandEnum> command = exchanger.exchange(null);
        engineSpeedSetup(command.getProperties().getCyclesSpeed());
        //TODO: improve
        switch (command.getType()){
            case RIGHT:
                return executeTurnByCycles(adjustCyclesByValue(true, command.getValue()), leftMotor, rightMotor);
//                return testRequest(correctedCycles, command);
            case LEFT:
//                return testRequest(correctedCycles, command);
                return executeTurnByCycles(adjustCyclesByValue(true, command.getValue()), rightMotor, leftMotor);
            case MOVE:
//                return testRequest(correctedCycles, command);
                return executeBothEnginesByCycles(adjustCyclesByValue(false, command.getValue()), rightMotor, leftMotor);
            case BACK:
//                return testRequest(correctedCycles, command);
                return executeBothEnginesByCycles((-1) * adjustCyclesByValue(false, command.getValue()), rightMotor, leftMotor);
            default:
                throw new ClientPlatformException("PLATFORM COMMAND= " + command );
        }
    }

    //Private Methods
    private boolean executeTurnByCycles(final int cycles, RegulatedMotor... engines){
        Future<Boolean> first = runEngine(engines[DEFAULT_0], DEFAULT_0);
        Future<Boolean> second = runEngine(engines[DEFAULT_1], cycles);
        try {
            return first.get() && second.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ClientException("executeTurnByCycles error: ", e);
        }
    }

    private boolean executeBothEnginesByCycles(int cycles, RegulatedMotor... engines){
        Future<Boolean> engineLeft = executeEngine(engines[DEFAULT_0], cycles);
        Future<Boolean> engineRight = executeEngine(engines[DEFAULT_1], cycles);

        try {
            return engineLeft.get() && engineRight.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ClientException("BothEnginesByCycles error: ", e);
        }
    }

    private Future<Boolean> runEngine(RegulatedMotor engine, int cycles){
        return executor.submit(() -> {
            lock.lock();
            try {
                switch(cycles){
                    case DEFAULT_0:
                        engine.stop(true);
                        break;
                    default:
                        engine.rotate(cycles);
                        break;
                }
            }finally {
                lock.unlock();
                System.out.println("ENGINE FINISHED = " + engine + "cycles= " + cycles);
            }
            return true;
        });
    }

    private Future<Boolean> executeEngine(RegulatedMotor engine, int cycles){
        return executor.submit(() -> {
            engine.rotate(cycles);
            return true;
        });
    }

    private void engineSpeedSetup(int cycleSpeed){
        rightMotor.setSpeed(cycleSpeed);
        leftMotor.setSpeed(cycleSpeed);
    }

}
