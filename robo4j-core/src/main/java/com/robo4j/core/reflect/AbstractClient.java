/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This AbstractClient.java  is part of robo4j.
 *
 *  robo4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  robo4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.reflect;

import com.robo4j.commons.annotation.RoboProvider;
import com.robo4j.core.client.ClientHTTPExecutor;
import com.robo4j.commons.registry.RegistryManager;
import com.robo4j.commons.annotation.RoboEngine;
import com.robo4j.commons.annotation.RoboSensor;
import com.robo4j.commons.annotation.RoboUnit;
import com.robo4j.commons.annotation.RoboService;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 09.06.2016
 */
public abstract class AbstractClient<FutureType>  {

    private static ExecutorService executor;
    protected AtomicBoolean active;
    protected AbstractClient(RoboReflectionScan scan){
        active = new AtomicBoolean(false);
        executor = new ClientHTTPExecutor();
        new RoboReflectiveInit(
                RegistryManager.getInstance().addAll(RegistryUtil.registry),
                scan.getClassesByAnnotation(RoboEngine.class),
                scan.getClassesByAnnotation(RoboSensor.class),
                scan.getClassesByAnnotation(RoboUnit.class),
                scan.getClassesByAnnotation(RoboService.class),
                scan.getClassesByAnnotation(RoboProvider.class)
        );
        active.set(true);
    }

    public Future<FutureType> submit(Callable<FutureType> task){
        return executor.submit(task);
    }

    public void end(){
        executor.shutdown();
    }
}
