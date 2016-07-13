/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This AbstractRoboReflective.java is part of robo4j.
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

package com.robo4j.brick.reflect;

import com.robo4j.brick.client.io.ClientException;
import com.robo4j.brick.engine.FrontHandEngine;
import com.robo4j.brick.engine.LeftEngine;
import com.robo4j.brick.engine.RightEngine;
import com.robo4j.brick.sensor.FrontHandTouchSensor;
import com.robo4j.brick.unit.FrontHandUnit;
import com.robo4j.brick.unit.PlatformUnit;
import com.robo4j.commons.annotation.RoboEngine;
import com.robo4j.commons.annotation.RoboSensor;
import com.robo4j.commons.annotation.RoboUnit;
import com.robo4j.commons.unit.DefaultUnit;
import com.robo4j.lego.control.LegoEngine;
import com.robo4j.lego.control.LegoSensor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Initial implementation of robo-client reflection usage
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 16.06.2016
 */

public abstract class AbstractRoboReflective {

    private volatile Map<String, LegoEngine> engineCache;
    private volatile Map<String, LegoSensor> sensorCache;
    private volatile Map<String, DefaultUnit> unitCache;

    protected AbstractRoboReflective() {
        engineCache = initCache();
        sensorCache = initSensorCache();
        unitCache = initUnitCache();
    }

    protected Map<String, LegoEngine> getEngineCache(){
        return engineCache;
    }

    protected Map<String, LegoSensor> getSensorCache(){
        return sensorCache;
    }

    protected Map<String, DefaultUnit> getUnitCache() {
        return unitCache;
    }

    //Private Methods
    @SuppressWarnings(value = "unchecked")
    private <EngineType extends LegoEngine> Map<String, EngineType> initCache(){
        try{
            final Map<String, EngineType> result = new HashMap<>();

            for (Iterator<?> iterator =
                 Arrays.asList(LeftEngine.class, RightEngine.class, FrontHandEngine.class).iterator(); iterator.hasNext();){
                Class<?> clazz = (Class<?>) iterator.next();
                if(clazz.isAnnotationPresent(RoboEngine.class)){
                    RoboEngine anno = clazz.getAnnotation(RoboEngine.class);
                    result.put(anno.value(), (EngineType) clazz.newInstance());
                }
            }
            return result;
        } catch (Exception e){
            throw new ClientException("ENGINE CACHE PROBLEM", e);
        }

    }

    @SuppressWarnings(value = "unchecked")
    private <SensorType extends LegoSensor> Map<String, SensorType> initSensorCache(){
        try{
            final Map<String, LegoSensor> result = new HashMap<>();
            for (Iterator<?> iterator =
                 Collections.singletonList(FrontHandTouchSensor.class).iterator(); iterator.hasNext();){
                Class<?> clazz = (Class<?>) iterator.next();
                if(clazz.isAnnotationPresent(RoboSensor.class)){
                    RoboSensor anno = clazz.getAnnotation(RoboSensor.class);
                    result.put(anno.value(), (SensorType) clazz.newInstance());
                }
            }
            return (Map<String, SensorType>) result;
        } catch (Exception e){
            throw new ClientException("SENSOR CACHE PROBLEM", e);
        }

    }


    @SuppressWarnings(value = "unchecked")
    private<UnitType extends DefaultUnit> Map<String, UnitType> initUnitCache(){
        try{
            final Map<String, DefaultUnit> result = new HashMap<>();
            for (Iterator<?> iterator =
                 Arrays.asList(FrontHandUnit.class, PlatformUnit.class).iterator(); iterator.hasNext();){
                Class<?> clazz = (Class<?>) iterator.next();
                System.out.println("RoboUnit initi start");
                if(clazz.isAnnotationPresent(RoboUnit.class)){
                    RoboUnit anno = clazz.getAnnotation(RoboUnit.class);
                    if(baseUnitCheck(anno, engineCache, sensorCache)){
                        System.out.println("init unit cache unit= " + anno.value());
                        result.put(anno.value(), (UnitType) clazz.newInstance());
                    } else {
                        System.out.println("NOT CONFIGURED UNIT anno= " + anno.value());
                        throw new ClientException("NOT CONFIGURED UNIT anno= " + anno.value());
                    }
                }
            }
            return (Map<String, UnitType>) result;
        } catch (Exception e){
            throw new ClientException("UNIT CACHE PROBLEM", e);
        }
    }

    //FIXME: platform problem
    private boolean baseUnitCheck(RoboUnit anno, final Map<String, LegoEngine> engineCache,
                                  final Map<String, LegoSensor> sensorCache){
        return true;
//        return engineCache.containsKey(anno.consumer()) && sensorCache.containsKey(anno.producer());
    }
}
