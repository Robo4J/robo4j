/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This CommandProviderImpl.java is part of robo4j.
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

package com.robo4j.brick.system;

import com.robo4j.brick.client.enums.RequestCommandEnum;
import com.robo4j.brick.client.io.ClientException;
import com.robo4j.brick.util.ConstantUtil;
import com.robo4j.commons.command.GenericCommand;
import com.robo4j.commons.concurrent.LegoThreadFactory;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.robotics.RegulatedMotor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by miroslavkopecky on 10/06/16.
 */
public class CommandProviderImpl implements CommandProvider {

    private static final int ROTATION_CYCLES = 9;
    private static final int DEFAULT_0 = 0;
    private static final int DEFAULT_1 = 1;

    private static volatile ExecutorService executorForCommands;
    private static volatile RegulatedMotor rightMotor;
    private static volatile RegulatedMotor leftMotor;
    private ReentrantLock lock;


    public CommandProviderImpl() {
        executorForCommands =  Executors.newFixedThreadPool(ConstantUtil.PLATFORM_ENGINES,
                new LegoThreadFactory(ConstantUtil.PROVIDER_BUS));
        engineInitiation();
        lock = new ReentrantLock();
    }

    @Override
    public boolean process(final GenericCommand<RequestCommandEnum> command) {

        engineSpeedSetup(command.getProperties().getCyclesSpeed());
        final int correctedCycles = adjustCyclesByValue(command.getValue());
        switch (command.getType()){
            case RIGHT:
                return executeTurnByCycles(correctedCycles, leftMotor, rightMotor);
            case LEFT:
                return executeTurnByCycles(correctedCycles, rightMotor, leftMotor);
            case MOVE:
                return executeBothEnginesByCycles(correctedCycles, rightMotor, leftMotor);
            case BACK:
                return executeBothEnginesByCycles((-1)* correctedCycles, rightMotor, leftMotor);
            case EXIT:
                break;
            default:
                throw new ClientException("PROCESS FAILURE NO SUCH COMMAND= " + command );
        }

        return false;
    }

    //Private Methods
    /**
     * Current adjustment for Angle and Distance
     *
     * @param value - value comes from command
     * @return - number of cycles
     */
    private int adjustCyclesByValue(String value){
        return ROTATION_CYCLES * Integer.valueOf(value);
    }

    private void engineInitiation(){
        rightMotor = new NXTRegulatedMotor(LocalEV3.get().getPort("B"));
        leftMotor = new NXTRegulatedMotor(LocalEV3.get().getPort("C"));
    }

    private void engineSpeedSetup(int cycleSpeed){
        rightMotor.setSpeed(cycleSpeed);
        leftMotor.setSpeed(cycleSpeed);
    }

    private boolean executeTurnByCycles(final int cycles, RegulatedMotor... engines){
        Future<Boolean> first = runEngine(engines[DEFAULT_0], DEFAULT_0);
        Future<Boolean> second = runEngine(engines[DEFAULT_1], cycles);
        try {
            return first.get() && second.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ClientException("executeTurnByCycles error: ", e);
        }
    }

    private Future<Boolean> runEngine(RegulatedMotor engine, int cycles){
        return executorForCommands.submit(() -> {
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
            }
            return true;
        });
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

    private Future<Boolean> executeEngine(RegulatedMotor engine, int cycles){
        return executorForCommands.submit(() -> {
            engine.rotate(cycles);
            return true;
        });
    }

}
