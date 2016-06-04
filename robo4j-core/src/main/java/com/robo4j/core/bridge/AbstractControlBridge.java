/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This AbstractControlBridge.java is part of robo4j.
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

package com.robo4j.core.bridge;

import com.robo4j.core.agent.AgentConsumer;
import com.robo4j.core.agent.AgentProducer;
import com.robo4j.core.agent.AgentStatus;
import com.robo4j.core.agent.GenericAgent;
import com.robo4j.core.agent.RoboAgent;
import com.robo4j.core.system.LegoThreadFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by miroslavkopecky on 28/05/16.
 */
public abstract class AbstractControlBridge<FutureType extends Callable> extends AbstractBridgeCache {

    private ExecutorService coreBus;
    private ExecutorService sensorBus;
    private ExecutorService guardianBus;
    protected volatile LinkedBlockingQueue<String> commandLineQueue;
    protected volatile AtomicBoolean activeThread;        // Core Bus is active
    protected volatile AtomicBoolean emergency;           // All stop
    protected volatile AtomicBoolean active;              // Control Pad

    protected AbstractControlBridge(){
        activeThread = new AtomicBoolean(false);
        active = new AtomicBoolean(false);
        emergency = new AtomicBoolean(false);
    }

    protected void initBridge() {
        guardianBus = Executors.newSingleThreadExecutor(new LegoThreadFactory(BridgeUtils.BUS_GUARDIAN_BUS));
        coreBus = Executors.newCachedThreadPool(new LegoThreadFactory(BridgeUtils.BUS_CORE_BUS));
        sensorBus = Executors.newCachedThreadPool(new LegoThreadFactory(BridgeUtils.BUS_SENSOR_BUS));
        commandLineQueue = new LinkedBlockingQueue<>();
    }

    public void submitToSensorBus(Runnable task){
        sensorBus.submit(task);
    }

    protected void putCommand(String c) throws InterruptedException{
        commandLineQueue.put(c);
    }

    protected RoboAgent getCoreBridgeAgent(final AgentProducer producer, final AgentConsumer consumer){
        final GenericAgent result = new GenericAgent(coreBus, producer, consumer);
        final AgentStatus status = result.activate();
        return result;
    }


    protected void coreBusDown(){
        coreBus.shutdown();
    }

    protected void coreBusDownNow(){
        coreBus.shutdownNow();
    }

    protected void sensorBusDown(){
        sensorBus.shutdown();
    }

    @SuppressWarnings(value = "unchecked")
    protected Future<FutureType> guardianBusSubmit(FutureType task){
        return guardianBus.submit(task);
    }

    protected void guardianBusExecute(Runnable task){
        guardianBus.execute(task);
    }

    protected void guardianBusDown(){
        guardianBus.shutdown();
    }
}
