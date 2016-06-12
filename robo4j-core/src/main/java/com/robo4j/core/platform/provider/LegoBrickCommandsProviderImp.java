/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LegoBrickCommandsProviderImp.java is part of robo4j.
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

package com.robo4j.core.platform.provider;

import com.robo4j.commons.concurrent.LegoThreadFactory;
import com.robo4j.core.control.LegoEngine;
import com.robo4j.core.lego.LegoBrickRemote;
import com.robo4j.core.platform.PlatformException;
import com.robo4j.core.platform.PlatformProperties;
import com.robo4j.core.platform.PlatformUtils;
import com.robo4j.core.platform.command.LegoCommandProperty;
import com.robo4j.core.platform.command.LegoPlatformCommandEnum;
import com.robo4j.core.system.dto.LegoEngineDTO;
import com.robo4j.core.system.enums.LegoAnalogPortEnum;
import com.robo4j.core.system.enums.LegoEngineEnum;
import com.robo4j.core.system.enums.LegoEnginePartEnum;
import lejos.remote.ev3.RMIRegulatedMotor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.robo4j.core.lego.rmi.LegoUnitProviderUtil.createRMIEngine;

/**
 *
 * Provider is implemented without closing
 *
 * Created by miroslavkopecky on 06/02/16.
 */

public class LegoBrickCommandsProviderImp implements LegoBrickCommandsProvider {

    private static final Logger logger = LoggerFactory.getLogger(LegoBrickCommandsProviderImp.class);
    private static final String RIGHT_ENGINE = "right";
    private static final String LEFT_ENGINE = "left";
    private static final int PLATFORM_ENGINES = 2;
    private static final int CENTIMETER_CYCLES = 33;
    private static final int ROTATION_CYCLES = 9;
    private static final int DEFAULT_0 = 0;
    private static final int DEFAULT_1 = 1;
    private static volatile ExecutorService executorForCommands;
    private static volatile RMIRegulatedMotor motorLeft;
    private static volatile RMIRegulatedMotor motorRight;
    private volatile LegoBrickRemote  legoBrickRemote;
    private volatile AtomicBoolean active;

    private ReentrantLock lock;
    private PlatformProperties properties;

    //@formatter:off
    public LegoBrickCommandsProviderImp(final LegoBrickRemote legoBrickRemote,
                                        final PlatformProperties properties,
                                        final Map<String, LegoEngine> engineCache){
    //@formatter:on
        this.active = new AtomicBoolean(false);
        this.legoBrickRemote = legoBrickRemote;
        this.properties = properties;

        executorForCommands = Executors.newFixedThreadPool(PLATFORM_ENGINES, new LegoThreadFactory(PlatformUtils.BUS_BRICK));

        engineCache.entrySet().stream()
                .filter(entry -> entry.getValue().getPart().equals(LegoEnginePartEnum.PLATFORM))
                .limit(PLATFORM_ENGINES)
                .forEach(entry -> {
                    switch (entry.getKey()){
                        case RIGHT_ENGINE:
                            motorRight = createEngine(legoBrickRemote, entry.getValue().getPort());
                            break;
                        case LEFT_ENGINE:
                            motorLeft = createEngine(legoBrickRemote, entry.getValue().getPort());
                            break;
                        default:
                            throw new PlatformException("NO SUCH ENGINE to INIT= " + entry);
                    }
                });

        lock = new ReentrantLock();
    }

    @Override
    public boolean process(LegoPlatformCommandEnum direction) {
        try{
            if((Objects.isNull(motorRight) || Objects.isNull(motorLeft)) && active.get()){
                return false;
            }
            logger.debug("Start Lego Movement direction= " + direction);
            motorLeft.setSpeed(properties.getCycles());
            motorRight.setSpeed(properties.getCycles());
            Future<Boolean> commandEngine = runEngineByDirection(direction);
            return commandEngine.get();
        } catch (RemoteException | InterruptedException | ExecutionException e){
            throw new PlatformException("RUN ERROR PROCESS: ", e);
        }

    }

    @Override
    public boolean process(LegoPlatformCommandEnum direction, LegoCommandProperty property) {
        if((Objects.isNull(motorRight) || Objects.isNull(motorLeft)) && active.get()){
            return false;
        }
        try{
            logger.debug("Start Lego Movement direction= " + direction + "property= " + property);
            motorLeft.setSpeed(properties.getCycles());
            motorRight.setSpeed(properties.getCycles());

            int cycles;
            switch (direction){
                case MOVE_CYCLES:
                    cycles = Integer.valueOf(property.getValue().trim());
                    return executeBothEnginesByCycles(cycles);
                case MOVE_DISTANCE:
                    cycles = Integer.valueOf(property.getValue().trim()) * CENTIMETER_CYCLES;
                    return executeBothEnginesByCycles(cycles);
                case BACK_DISTANCE:
                    cycles = -Integer.valueOf(property.getValue().trim()) * CENTIMETER_CYCLES;
                    return executeBothEnginesByCycles(cycles);
                case LEFT_CYCLES:
                    cycles = (Integer.valueOf(property.getValue().trim()) * ROTATION_CYCLES);
                    return executeTurnByCycles(cycles, motorRight, motorLeft);
                case RIGHT_CYCLES:
                    cycles = (Integer.valueOf(property.getValue().trim()) * ROTATION_CYCLES);
                    return executeTurnByCycles(cycles, motorLeft, motorRight);
                default:
                    throw new PlatformException("NO SUCH COMMAND direction= " + direction + " property= " + property);
            }
        } catch (RemoteException e){
            throw new PlatformException("RUN ERROR: ", e);
        }

    }

    //Private Methods
    private Future<Boolean> runEngineByDirection(LegoPlatformCommandEnum direction){
        return executorForCommands.submit(() -> {
            switch (direction){
                case MOVE:
                    motorRight.forward();
                    motorLeft.forward();
                    break;
                case BACK:
                    motorRight.backward();
                    motorLeft.backward();
                    break;
                case LEFT:
                    motorLeft.forward();
                    motorRight.stop(true);
                    break;
                case RIGHT:
                    motorRight.forward();
                    motorLeft.stop(true);
                    break;
                case STOP:
                    motorLeft.stop(true);
                    motorRight.stop(true);
                    break;
                case INIT:
                    active.set(true);
                    break;
                case CLOSE:
                case EXIT:
                    logger.info("EXIT/CLOSE COMMAND CALLED");
                    motorLeft.close();
                    motorRight.close();
                    executorForCommands.shutdown();
                    active.set(false);
                    break;
                case EMERGENCY_STOP:
                    motorLeft.stop(true);
                    motorRight.stop(true);
                    break;
                default:
                    throw new PlatformException("NO SUCH COMMAND direction= " + direction);
            }
            return true;
        });
    }

    private Future<Boolean> runEngine(RMIRegulatedMotor engine, int cycles, Condition condition){
        return executorForCommands.submit(() -> {
            lock.lock();
            try {
                switch(cycles){
                    case DEFAULT_0:
                        logger.info("Engine STOP");
                        engine.stop(true);
                        condition.await();
                        break;
                    default:
                        logger.info("Engine RUN");
                        engine.rotate(cycles);
                        condition.signal();
                        break;
                }

            } catch (RemoteException | InterruptedException e) {
                throw new PlatformException("Command error: ", e);
            }finally {
                lock.unlock();
            }
            return true;
        });
    }


    private boolean executeTurnByCycles(int cycles, RMIRegulatedMotor... engines){
        Condition engineActive = lock.newCondition();

        Future<Boolean> first = runEngine(engines[DEFAULT_0], DEFAULT_0, engineActive);
        Future<Boolean> second = runEngine(engines[DEFAULT_1], cycles, engineActive);
        try {
            return first.get() && second.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new PlatformException("executeTurnByCycles error: ", e);
        }
    }


    private Future<Boolean> executeEngine(RMIRegulatedMotor engine, int cycles){
        return executorForCommands.submit(() -> {
            try {
                engine.rotate(cycles);
            } catch (RemoteException e) {
                throw new PlatformException("Command error: ", e);
            }
            return true;
        });
    }

    private boolean executeBothEnginesByCycles(int cycles){

        logger.info("executeBothEnginesByCycles = " + cycles);

        Future<Boolean> engineLeft = executeEngine(motorLeft, cycles);
        Future<Boolean> engineRight = executeEngine(motorRight, cycles);

        try {
            return engineLeft.get() && engineRight.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new PlatformException("BothEnginesByCycles error: ", e);
        }
    }

    private RMIRegulatedMotor createEngine(final LegoBrickRemote  legoBrickRemote, final LegoAnalogPortEnum port ){
        final LegoEngineDTO engine = new LegoEngineDTO(port, LegoEngineEnum.NXT);
        return createRMIEngine(legoBrickRemote, engine);
    }


    @Override
    public void exit() throws RemoteException, InterruptedException {
        logger.info("MAIN EXIT COMMAND CALLED");
        motorLeft.close();
        motorRight.close();
    }

    @Override
    public boolean isActive() {
        return active.get();
    }
}
