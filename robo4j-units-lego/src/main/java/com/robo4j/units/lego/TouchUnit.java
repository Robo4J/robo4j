/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.units.lego;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.concurrency.RoboThreadFactory;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.hw.lego.ILegoSensor;
import com.robo4j.hw.lego.enums.DigitalPortEnum;
import com.robo4j.hw.lego.enums.SensorTypeEnum;
import com.robo4j.hw.lego.provider.SensorProvider;
import com.robo4j.hw.lego.wrapper.SensorWrapper;
import com.robo4j.units.lego.platform.MotorRotationEnum;
import com.robo4j.units.lego.utils.LegoUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class TouchUnit extends RoboUnit<Boolean> {

    private static final String PRESSED = "1.0";
    private static final String RELEASED = "0.0";
    private ExecutorService executor = new ThreadPoolExecutor(LegoUtils.SINGLE_THREAD_POOL_SIZE,
            LegoUtils.SINGLE_THREAD_POOL_SIZE, LegoUtils.KEEP_ALIVE_TIME, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), new RoboThreadFactory("Robo4J Lego Touch Unit", true));
    private volatile AtomicBoolean sensorActive = new AtomicBoolean(false);
    private volatile MotorRotationEnum currentState;
    private volatile ILegoSensor sensor;
    private String target;

    public TouchUnit(RoboContext context, String id) {
        super(Boolean.class, context, id);
    }

    @Override
    public void shutdown() {
        setState(LifecycleState.SHUTTING_DOWN);
        try {
            sensorActive.set(false);
            executor.awaitTermination(LegoUtils.TERMINATION_TIMEOUT, TimeUnit.SECONDS);
            executor.shutdown();
        } catch (InterruptedException e) {
            SimpleLoggingUtil.error(getClass(), "termination failed");
        }
        sensor.close();
        if (executor.isShutdown()) {
            SimpleLoggingUtil.debug(getClass(), "executor is down");
        }
        setState(LifecycleState.SHUTDOWN);
    }


    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        setState(LifecycleState.UNINITIALIZED);
        target = configuration.getString("target", null);
        if (target == null) {
            throw ConfigurationException.createMissingConfigNameException("target");
        }

        String sensorType = configuration.getString("touchSensorPort", DigitalPortEnum.S1.getType());
        SensorProvider provider = new SensorProvider();
        sensor = new SensorWrapper<>(provider, DigitalPortEnum.getByType(sensorType), SensorTypeEnum.TOUCH);

        sensorActive.set(true);
        currentState = MotorRotationEnum.STOP;

        setState(LifecycleState.INITIALIZED);
        runTouchSensor();
    }

    //private methods
    private void runTouchSensor(){
        executor.submit(() -> {
           while(sensorActive.get()){
               if(sensor.getData().equals(PRESSED) && currentState == MotorRotationEnum.STOP){
                   getContext().getReference(target).sendMessage(MotorRotationEnum.FORWARD);
                   currentState = MotorRotationEnum.FORWARD;
               }

               if(sensor.getData().equals(RELEASED) && currentState == MotorRotationEnum.FORWARD){
                   getContext().getReference(target).sendMessage(MotorRotationEnum.STOP);
                   currentState = MotorRotationEnum.STOP;
               }
           }
        });
    }

}
