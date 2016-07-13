/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This UnitCache.java is part of robo4j.
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

package com.robo4j.core.unit;

import com.robo4j.commons.annotation.RoboUnit;
import com.robo4j.core.control.DefaultSystemConfig;
import com.robo4j.core.lego.LegoBrickPropertiesHolder;
import com.robo4j.lego.control.LegoUnit;
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
 * @author Miro Kopecky (@miragemiko)
 * @since 01.07.2016
 */
public class UnitCache implements DefaultSystemConfig {

    private volatile static UnitCache INSTANCE;
    private volatile static Map<String, LegoUnit> cache;

    private UnitCache(LegoBrickPropertiesHolder holder) {
        cache = initCache(holder, RoboUnit.class);
    }

    public Map<String, LegoUnit> getCache() {
        return cache;
    }

    public LegoUnit getUnitByName(final String name) {
        return cache.get(name);
    }

    public static UnitCache getInstance(LegoBrickPropertiesHolder holder) {
        if (INSTANCE == null) {
            synchronized (UnitCache.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UnitCache(holder);
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
    private <Annotation extends RoboUnit, UnitType extends LegoUnit> Map<String, UnitType> initCache(
            LegoBrickPropertiesHolder holder, Class<Annotation> annotation) {
        try {
            final Map<String, LegoUnit> result = new HashMap<>();

            //FIXME: to be removed
            String unitPackage = holder.getUnitPackage();
            if(Objects.isNull(unitPackage) || unitPackage.isEmpty()){
                unitPackage = "com.robo4j.core.fronthand";
            }

            if (validateConfiguration(holder)) {

                Reflections reflections = new Reflections(new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(unitPackage))
                        .setExecutorService(Executors.newFixedThreadPool(REFLECTION_THREADS))
                        .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner())
                );
                Set<Class<?>> classes = reflections.getTypesAnnotatedWith(annotation);
                reflections.getConfiguration().getExecutorService().shutdown();

                for (Class c : classes) {
                    Annotation roboUnit = (Annotation) c.getAnnotation(annotation);
                    UnitType unit = (UnitType) c.getConstructor().newInstance();
                    result.put(roboUnit.value(), unit);
                }
            }

            return (Map<String, UnitType>) result;
        } catch (Exception e) {
            throw new UnitException("UNIT CACHE PROBLEM", e);
        }
    }

    /* Util method to check the configuration */
    private boolean validateConfiguration(final LegoBrickPropertiesHolder holder) {
//        return  Objects.nonNull(holder.getUnitPackage()) &&
          return  Objects.nonNull(holder.getEnginePackage()) &&
                  Objects.nonNull(holder.getSensorPackage());
    }



//    private <Annotation extends RoboUnit, UnitType extends LegoUnit> boolean validateUnitConfiguration(
//            final LegoBrickPropertiesHolder holder, final Annotation anno, final UnitType unit){
//        return
//                Objects.nonNull(unit.getUnitName()) &&
//                Objects.nonNull(unit.getProducerName()) &&
//                Objects.nonNull(unit.getConsumerName());
//
//    }


    @Override
    public String toString() {
        return "UnitCache{" +
                "cache= " + cache +
                "}";
    }
}
