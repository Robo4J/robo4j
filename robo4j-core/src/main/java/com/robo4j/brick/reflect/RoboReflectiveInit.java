/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This RoboReflectiveInit.java  is part of robo4j.
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

package com.robo4j.brick.reflect;

import com.robo4j.brick.client.io.ClientException;
import com.robo4j.brick.manager.RegistryManager;
import com.robo4j.commons.annotation.RoboEngine;
import com.robo4j.commons.annotation.RoboSensor;
import com.robo4j.commons.annotation.RoboUnit;
import com.robo4j.commons.annotation.RoboService;
import com.robo4j.commons.enums.RegistryTypeEnum;
import com.robo4j.commons.motor.GenericMotor;
import com.robo4j.commons.sensor.GenericSensor;
import com.robo4j.commons.service.GenericService;
import com.robo4j.commons.unit.GenericUnit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Initial implementation of robo-client reflection usage
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 16.06.2016
 */

class RoboReflectiveInit {

    @SuppressWarnings(value = "unchecked")
    RoboReflectiveInit(
                RegistryManager registryManager,
                Stream<Class<?>> engines,
                Stream<Class<?>> sensors,
                Stream<Class<?>> units,
                Stream<Class<?>> services) {

        /* initiation of all caches used at the run time */

        if(registryManager.isActive()){
            registryManager.initRegistry(RegistryTypeEnum.ENGINES, initEngineCache(engines));
            registryManager.initRegistry(RegistryTypeEnum.SENSORS, initSensorCache(sensors));
            registryManager.initRegistry(RegistryTypeEnum.UNITS, initUnitCache(units));
            registryManager.initRegistry(RegistryTypeEnum.SERVICES, initServiceCache(services));
        }

    }



    //Private Methods
    @SuppressWarnings(value = "unchecked")
    private <EngineType extends GenericMotor> Map<String, EngineType> initEngineCache(Stream<Class<?>> engines){
        try{
            final Map<String, EngineType> result = new HashMap<>();

            for (Iterator<?> iterator =
                 engines.iterator(); iterator.hasNext();){
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
    private <SensorType extends GenericSensor> Map<String, SensorType> initSensorCache(Stream<Class<?>> sensors){
        try{
            final Map<String, SensorType> result = new HashMap<>();
            for (Iterator<?> iterator =
                 sensors.iterator(); iterator.hasNext();){
                Class<?> clazz = (Class<?>) iterator.next();
                if(clazz.isAnnotationPresent(RoboSensor.class)){
                    RoboSensor anno = clazz.getAnnotation(RoboSensor.class);
                    result.put(anno.value(), (SensorType) clazz.newInstance());
                }
            }
            return result;
        } catch (Exception e){
            throw new ClientException("SENSOR CACHE PROBLEM", e);
        }

    }

    //TODO: FIMXE -> create logic to check hardware resources
    @SuppressWarnings(value = "unchecked")
    private<UnitType extends GenericUnit>  Map<String, UnitType> initUnitCache(Stream<Class<?>> units){
        try{
            final Map<String, UnitType> result = new HashMap<>();
            for (Iterator<?> iterator = units.iterator(); iterator.hasNext();){
                Class<?> clazz = (Class<?>) iterator.next();
                if(clazz.isAnnotationPresent(RoboUnit.class)){
                    RoboUnit anno = clazz.getAnnotation(RoboUnit.class);
                    result.put(anno.value(), (UnitType) clazz.newInstance());
                }
            }
            return result;
        } catch (InstantiationException | IllegalAccessException e){
            throw new ClientException("UNIT CACHE PROBLEM", e);
        }
    }

    @SuppressWarnings(value = "unchecked")
    private<ServiceType extends GenericService> Map<String, ServiceType> initServiceCache(Stream<Class<?>> services) {
        try{
            final Map<String, ServiceType> result = new HashMap<>();
            for (Iterator<?> iterator = services.iterator(); iterator.hasNext();){
                Class<?> clazz = (Class<?>) iterator.next();
                if(clazz.isAnnotationPresent(RoboService.class)){
                    RoboService anno = clazz.getAnnotation(RoboService.class);
                    result.put(anno.value(), (ServiceType)clazz.newInstance());
                }
            }
            return result;
        } catch (InstantiationException | IllegalAccessException e){
            throw new ClientException("SERVICE CACHE PROBLEM", e);
        }
    }


}
