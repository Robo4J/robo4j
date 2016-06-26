/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This AbstractBridgeCache.java is part of robo4j.
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

import com.robo4j.core.bridge.command.cache.BatchCommand;
import com.robo4j.core.bridge.command.cache.CommandCache;
import com.robo4j.core.engines.EngineCache;
import com.robo4j.core.lego.LegoBrickPropertiesHolder;
import com.robo4j.core.sensor.SensorCache;

import java.util.Objects;


/**
 * Abstract class for robot bridge
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 28.05.2016
 */
public abstract class AbstractBridgeCache {

    protected CommandCache commandCache;
    protected EngineCache engineCache;
    protected SensorCache sensorCache;


    protected void initCache(LegoBrickPropertiesHolder holder) {
        commandCache = CommandCache.getInstance(holder);
        engineCache = EngineCache.getInstance(holder);
        sensorCache = SensorCache.getInstance(holder);
    }

    protected boolean commandCacheActive(){
        return Objects.nonNull(commandCache);
    }

    protected boolean engineCacheActive(){
        return Objects.nonNull(engineCache);
    }

    protected boolean sensorCacheActive(){
        return Objects.nonNull(sensorCache);
    }

    protected void addGenericCommand(BatchCommand command){
        commandCache.addGenericCommand(command);
    }

    protected BatchCommand getGenericCommand(String seq){
        return commandCache.getCache().get(seq);
    }

}
