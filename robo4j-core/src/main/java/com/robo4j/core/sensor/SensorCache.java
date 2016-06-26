/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This SensorCache.java is part of robo4j.
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

package com.robo4j.core.sensor;

import com.robo4j.commons.annotation.RoboSensor;
import com.robo4j.core.control.DefaultSystemConfig;
import com.robo4j.core.engines.EngineCacheException;
import com.robo4j.core.lego.LegoBrickPropertiesHolder;
import com.robo4j.lego.control.LegoSensor;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Sensor cache is unique for RoboSystem
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 26.06.2016
 */
public class SensorCache implements DefaultSystemConfig {

    private volatile static SensorCache INSTANCE;
    private volatile static Map<String, LegoSensor> cache;

    private SensorCache(LegoBrickPropertiesHolder legoBrickPropertiesHolder){
        cache = initCache(legoBrickPropertiesHolder, RoboSensor.class);
    }

    public Map<String, LegoSensor> getCache() {
        return cache;
    }

    public LegoSensor getSensorByName(final String name){
        return cache.get(name);
    }

    public static SensorCache getInstance(LegoBrickPropertiesHolder legoBrickPropertiesHolder){
        if(INSTANCE == null ){
            synchronized (SensorCache.class){
                if(INSTANCE == null ){
                    INSTANCE = new SensorCache(legoBrickPropertiesHolder);
                }
            }
        }
        return INSTANCE;
    }

    //Private Methods

    /**
     * Based on available date sensor cache is initiated
     * when no data cache is empty
     */
    @SuppressWarnings(value = "unchecked")
    private <Annotation extends RoboSensor, SensorType extends LegoSensor> Map<String, SensorType>
    initCache(LegoBrickPropertiesHolder holder, Class<Annotation> annotation){
        try{
            final Map<String, LegoSensor> result = new HashMap<>();
            if(validateConfiguration(holder)){
                Reflections reflections = new Reflections(new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(holder.getSensorPackage()))
                        .setExecutorService(Executors.newFixedThreadPool(REFLECTION_THREADS))
                        .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner())
                );
                Set<Class<?>> classes = reflections.getTypesAnnotatedWith(annotation);
                reflections.getConfiguration().getExecutorService().shutdown();

                for(Class c: classes){
                    Annotation roboSensor = (Annotation)c.getAnnotation(annotation);
                    SensorType sensor = (SensorType) c.getConstructor().newInstance();
                    if(Objects.isNull(sensor.getSensor()) || Objects.isNull(sensor.getPort())){
                        throw new EngineCacheException("CACHE SENSOR NOT VALID = " + sensor);
                    } else {
                        result.put(roboSensor.value(), sensor);
                    }
                }
            }
            return (Map<String, SensorType>)result;
        } catch (Exception e){
            throw new SensorException("SENSOR CACHE PROBLEM", e);
        }
    }

    /* Util method to check the configuration */
    private boolean validateConfiguration(final LegoBrickPropertiesHolder holder){
        return Objects.nonNull(holder.getSensorPackage()) && !holder.getSensorPackage().isEmpty();
    }

}
