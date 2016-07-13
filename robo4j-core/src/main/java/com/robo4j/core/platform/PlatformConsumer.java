/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This PlatformConsumer.java is part of robo4j.
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

import com.robo4j.commons.agent.AgentConsumer;
import com.robo4j.commons.command.GenericCommand;
import com.robo4j.commons.concurrent.CoreBusQueue;
import com.robo4j.core.platform.command.LegoPlatformCommandEnum;
import com.robo4j.core.system.dto.LegoEngineDTO;
import com.robo4j.lego.control.LegoBrickRemote;
import com.robo4j.lego.control.LegoEngine;
import com.robo4j.lego.enums.LegoAnalogPortEnum;
import com.robo4j.lego.enums.LegoEngineEnum;
import com.robo4j.lego.enums.LegoEnginePartEnum;
import lejos.remote.ev3.RMIRegulatedMotor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import static com.robo4j.core.lego.rmi.LegoUnitProviderUtil.createRMIEngine;
import static com.robo4j.lego.util.LegoPlatformUtil.adjustCyclesByValue;

/**
 * Core-robo Platform Consumer - responsible for interaction with hardware
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 06.07.2016
 */
public class PlatformConsumer implements AgentConsumer, Callable<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(PlatformConsumer.class);
    private static final int DEFAULT_0 = 0;
    private static final int DEFAULT_1 = 1;
    private static final String RIGHT = "right";
    private static final String LEFT = "left";
    private static final int PLATFORM_ENGINES = 2;

    private ExecutorService executor;
    private Exchanger<GenericCommand<LegoPlatformCommandEnum>> exchanger;
    private volatile RMIRegulatedMotor rightMotor;
    private volatile RMIRegulatedMotor leftMotor;
    private ReentrantLock lock;
    //TODO: need to implement properly platform properties -> refactor

    public PlatformConsumer(final LegoBrickRemote legoBrickRemote,
                            final ExecutorService executor,
                            final Exchanger<GenericCommand<LegoPlatformCommandEnum>> exchanger,
                            final Map<String, LegoEngine> engineCache ) {

        this.executor = executor;
        this.lock = new ReentrantLock();
        this.exchanger = exchanger;

        //TODO: can be improved
        engineCache.entrySet().stream()
                .filter(entry -> entry.getValue().getPart().equals(LegoEnginePartEnum.PLATFORM))
                .limit(PLATFORM_ENGINES)
                .forEach(entry -> {
                    switch (entry.getKey()){
                        case RIGHT:
                            rightMotor = createEngine(legoBrickRemote, entry.getValue().getPort());
                            break;
                        case LEFT:
                            leftMotor = createEngine(legoBrickRemote, entry.getValue().getPort());
                            break;
                        default:
                            throw new PlatformException("NO SUCH ENGINE to INIT= " + entry);
                    }
                });

    }

    @Override
    public void setMessageQueue(CoreBusQueue commandsQueue) {
        throw new PlatformException("NOT IMPLEMENTED messageQueue");
    }

    /**
     * if command doesn't contain information about the cycles, then
     * is automatically taken as ACTIVE_COMMAND -> move directly without stop
     * command with property getSpeedCycles => DIRECT_COMMAND, BATCH_COMMAND, COMPLEX_COMMAND
     */
    @Override
    public Boolean call() throws Exception {
        final GenericCommand<LegoPlatformCommandEnum> command = exchanger.exchange(null);
        logger.info("RECEIVED COMMAND = " + command);

        boolean active = Objects.isNull(command.getValue()) || command.getValue().isEmpty();
        engineSpeedSetup(command.getProperties().getCyclesSpeed());

        if(active){
            return runEngineByDirection(command.getType()).get();
        } else {
            switch (command.getType()){
                case RIGHT:
                    return executeTurnByCycles(adjustCyclesByValue(true, command.getValue()), leftMotor, rightMotor);
                case LEFT:
                    return executeTurnByCycles(adjustCyclesByValue(true, command.getValue()), rightMotor, leftMotor);
                case MOVE:
                    return executeBothEnginesByCycles(adjustCyclesByValue(false, command.getValue()), rightMotor, leftMotor);
                case BACK:
                    return executeBothEnginesByCycles((-1) * adjustCyclesByValue(false, command.getValue()), rightMotor, leftMotor);
                default:
                    throw new PlatformException("NO SUCH COMMAND for ENGINE: " + command);
            }
        }



    }

    //Private Methods
    private Future<Boolean> runEngineByDirection(LegoPlatformCommandEnum direction){
        return executor.submit(() -> {
            switch (direction){
                case MOVE:
                    rightMotor.forward();
                    leftMotor.forward();
                    break;
                case BACK:
                    rightMotor.backward();
                    leftMotor.backward();
                    break;
                case LEFT:
                    leftMotor.forward();
                    rightMotor.stop(true);
                    break;
                case RIGHT:
                    rightMotor.forward();
                    leftMotor.stop(true);
                    break;
                case STOP:
                    leftMotor.stop(true);
                    rightMotor.stop(true);
                    break;
                case EXIT:
                case CLOSE:
                    logger.info("EXIT/CLOSE COMMAND CALLED");
                    leftMotor.close();
                    rightMotor.close();
                    break;
                default:
                    throw new PlatformException("NO SUCH COMMAND direction= " + direction);
            }
            return true;
        });
    }

    private boolean executeTurnByCycles(final int cycles, RMIRegulatedMotor... engines){
        Future<Boolean> first = runEngine(engines[DEFAULT_0], DEFAULT_0);
        Future<Boolean> second = runEngine(engines[DEFAULT_1], cycles);
        try {
            return first.get() && second.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new PlatformException("executeTurnByCycles error: ", e);
        }
    }

    private boolean executeBothEnginesByCycles(int cycles, RMIRegulatedMotor... engines){
        Future<Boolean> engineLeft = executeEngine(engines[DEFAULT_0], cycles);
        Future<Boolean> engineRight = executeEngine(engines[DEFAULT_1], cycles);

        try {
            return engineLeft.get() && engineRight.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new PlatformException("BothEnginesByCycles error: ", e);
        }
    }

    private Future<Boolean> executeEngine(RMIRegulatedMotor engine, int cycles){
        return executor.submit(() -> {
            engine.rotate(cycles);
            return true;
        });
    }

    private Future<Boolean> runEngine(RMIRegulatedMotor engine, int cycles){
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
                logger.info("ENGINE FINISHED = " + engine + "cycles= " + cycles);
            }
            return true;
        });
    }

    @SuppressWarnings(value = "unchecked")
    private RMIRegulatedMotor createEngine(final LegoBrickRemote  legoBrickRemote, final LegoAnalogPortEnum port ){
        final LegoEngineDTO engine = new LegoEngineDTO(port, LegoEngineEnum.NXT);
        return createRMIEngine(legoBrickRemote, engine);
    }

    private void engineSpeedSetup(int cycleSpeed) throws RemoteException {
        rightMotor.setSpeed(cycleSpeed);
        leftMotor.setSpeed(cycleSpeed);
    }


}
