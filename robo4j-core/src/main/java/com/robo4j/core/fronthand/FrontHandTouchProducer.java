/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This FrontHandTouchProducer.java is part of robo4j.
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

import com.robo4j.commons.agent.AgentProducer;
import com.robo4j.commons.concurrent.CoreBusQueue;
import lejos.hardware.sensor.EV3TouchSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Exchanger;

/**
 *
 * maybe Exchanger should be removed
 * Lego oriented
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 27.04.2016
 */
public class FrontHandTouchProducer implements AgentProducer, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(FrontHandTouchProducer.class);

    private volatile EV3TouchSensor touchSensor;
    private Exchanger<Boolean> exchanger;

    /* Represent signal producer */

    public FrontHandTouchProducer(Exchanger<Boolean> exchanger, EV3TouchSensor touchSensor) {
        this.exchanger = exchanger;
        this.touchSensor = touchSensor;
    }

    public EV3TouchSensor getTouchSensor() {
        return touchSensor;
    }

    @Override
    public void run() {
        boolean state = getTouchSensorPressState(touchSensor);
        logger.info("TOUCH STARTS");
        try {
            exchanger.exchange(state);
        } catch (InterruptedException e) {
            throw new FrontHandException("TOUCH PRODUCER e", e);
        } finally {
            logger.info("TOUCH ENDS thread= " + Thread.currentThread().getName());
        }
    }

    @Override
    public CoreBusQueue getMessageQueue() {
        return null;
    }

    //Private Methods
    private boolean getTouchSensorPressState(EV3TouchSensor touchSensor){
        logger.debug("getTouchSensorPressState touchSensor= {}", touchSensor.getName());
        boolean touched = false;

        int sampleSize = touchSensor.getTouchMode().sampleSize();
        final float[] samples = new float[sampleSize];
        touchSensor.fetchSample(samples, 0);
        if(samples[0] == 1F){
            touched = true;
        }
        logger.debug("getTouchSensorPressState touched= {}", touched);
        return touched;
    }
}
