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

import java.util.Objects;

/**
 * Created by miroslavkopecky on 28/05/16.
 */
public abstract class AbstractBridgeCache {

    protected CommandCache commandCache;
    protected EngineCache engineCache;


    protected void initCache(LegoBrickPropertiesHolder holder) {
        commandCache = CommandCache.getInstance(holder);
        engineCache = EngineCache.getInstance(holder);
    }

    protected boolean commandCacheActive(){
        return Objects.nonNull(commandCache);
    }

    protected boolean engineCacheActive(){
        return Objects.nonNull(engineCache);
    }

    protected void addGenericCommand(BatchCommand command){
        commandCache.addGenericCommand(command);
    }

    protected BatchCommand getGenericCommand(String seq){
        return commandCache.getCache().get(seq);
    }

}
