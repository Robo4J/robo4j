/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LegoFrontHandProviderImp.java is part of robo4j.
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

import com.robo4j.commons.concurrent.LegoThreadFactory;
import com.robo4j.core.fronthand.command.FrontHandCommandEnum;
import com.robo4j.core.lego.LegoBrickRemote;
import com.robo4j.core.system.dto.LegoEngineDTO;
import com.robo4j.lego.control.LegoEngine;
import com.robo4j.lego.enums.LegoAnalogPortEnum;
import com.robo4j.lego.enums.LegoEngineEnum;
import com.robo4j.lego.enums.LegoEnginePartEnum;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.remote.ev3.RMIRegulatedMotor;

import java.rmi.RemoteException;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.robo4j.core.lego.rmi.LegoUnitProviderUtil.createRMIEngine;
import static com.robo4j.core.lego.rmi.LegoUnitProviderUtil.createTouchSensor;

/**
 *
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 27/04/16
 */
public class LegoFrontHandProviderImp implements LegoFrontHandProvider {

    /* all connected pars  */
    private static final int CONNECTED_ELEMENTS = 2;
    private static final int DEFAULT_SPEED = 300;

    private static volatile ExecutorService executorForCommands;
    private volatile EV3TouchSensor touchSensor;
    private volatile RMIRegulatedMotor motorHandPortA;
    private volatile LegoBrickRemote legoBrickRemote;
    private volatile AtomicBoolean active;
    private Exchanger<Boolean> exchanger;



    public LegoFrontHandProviderImp(final LegoBrickRemote legoBrickRemote,
                                    final Map<String, LegoEngine> engineCache) {
        this.active = new AtomicBoolean(false);
        this.legoBrickRemote = legoBrickRemote;
        this.exchanger = new Exchanger<>();
        executorForCommands = Executors.newFixedThreadPool(CONNECTED_ELEMENTS, new LegoThreadFactory(FrontHandUtils.BUS_FRONT_HAND));

        engineCache.entrySet().stream()
                .filter(entry -> entry.getValue().getPart().equals(LegoEnginePartEnum.HAND))
                .forEach(entry ->
                    motorHandPortA = createEngine(legoBrickRemote, entry.getValue().getPort())
                );

        touchSensor = createTouchSensor(legoBrickRemote);
    }

    @Override
    public boolean process(FrontHandCommandEnum command){

        try {
            if((Objects.isNull(motorHandPortA) || Objects.isNull(legoBrickRemote)) && active.get()){
                return false;
            }

            switch (command){
                case COMMAND:
                    motorHandPortA.setSpeed(DEFAULT_SPEED);
                    active.set(true);
                    executorForCommands.execute(new FrontHandTouchProducer(exchanger, touchSensor));
                    final Future<Boolean> engineActive = executorForCommands.submit(new FrontHandEngineConsumer(exchanger, motorHandPortA));
                    try {
                        active.set(engineActive.get());
                    } catch (InterruptedException | ConcurrentModificationException | ExecutionException e) {
                        throw new FrontHandException("SOMETHING ERROR CYCLE command= " + command, e);
                    }
                    break;
                case EXIT:
                    if(Objects.nonNull(motorHandPortA)){
                        motorHandPortA.close();
                    }
                    if(Objects.nonNull(touchSensor)){
                        touchSensor.close();
                    }
                    executorForCommands.shutdown();
                    active.set(false);
                    break;
                default:
                    throw new FrontHandException("SOMETHING WRONG NO command= " + command);
            }
            return true;
        }catch (RemoteException e){
            throw new FrontHandException("RUN ERROR PROCESS: ", e);
        }

    }

    @Override
    public boolean isActive() {
        return active.get();
    }

    //Private Methods
    private RMIRegulatedMotor createEngine(final LegoBrickRemote legoBrickRemote, final LegoAnalogPortEnum type ){
        return createRMIEngine(legoBrickRemote, new LegoEngineDTO(type, LegoEngineEnum.MEDIUM));
    }
}
