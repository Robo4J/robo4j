/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This AbstractRoboClient.java is part of robo4j.
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

package com.robo4j.brick.client;

import com.robo4j.brick.client.io.ClientException;
import com.robo4j.brick.engine.LeftEngine;
import com.robo4j.brick.engine.RightEngine;
import com.robo4j.commons.annotation.RoboEngine;
import com.robo4j.lego.control.LegoEngine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by miroslavkopecky on 14/06/16.
 */
public abstract class AbstractRoboClient {

    private volatile static Map<String, LegoEngine> engineCache;

    AbstractRoboClient() {
        engineCache = initCache();
    }

    protected  Map<String, LegoEngine> getEngineCache(){
        return engineCache;
    }


    //Private Methods
    @SuppressWarnings(value = "unchecked")
    private <EngineType extends LegoEngine> Map<String, EngineType> initCache(){
        try{
            final Map<String, LegoEngine> result = new HashMap<>();

            for (Iterator<?> iterator =
                 Arrays.asList(LeftEngine.class, RightEngine.class).iterator(); iterator.hasNext();){
                Class<?> clazz = (Class<?>) iterator.next();
                if(clazz.isAnnotationPresent(RoboEngine.class)){
                    RoboEngine anno = clazz.getAnnotation(RoboEngine.class);
                    result.put(anno.value(), (LegoEngine) clazz.newInstance());
                }
            }
            return (Map<String, EngineType>)result;
        } catch (Exception e){
            throw new ClientException("ENGINE CACHE PROBLEM", e);
        }

    }

}
