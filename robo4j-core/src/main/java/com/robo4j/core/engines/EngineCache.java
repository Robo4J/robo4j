/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This EngineCache.java is part of robo4j.
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

package com.robo4j.core.engines;

import com.robo4j.core.annotation.RoboEngine;
import com.robo4j.core.control.DefaultSystemConfig;
import com.robo4j.core.control.LegoEngine;
import com.robo4j.core.lego.LegoBrickPropertiesHolder;
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
 * Engine configuration is unique
 *
 * Created by miroslavkopecky on 05/05/16.
 */
public class EngineCache implements DefaultSystemConfig {

    private volatile static EngineCache INSTANCE;
    private volatile static Map<String, LegoEngine> cache;

    private EngineCache(LegoBrickPropertiesHolder legoBrickPropertiesHolder){
        cache = initCache(legoBrickPropertiesHolder, RoboEngine.class);
    }

    public Map<String, LegoEngine> getCache() {
        return cache;
    }

    public LegoEngine getEngineByName(final String name){
        return cache.get(name);
    }

    public static EngineCache getInstance(LegoBrickPropertiesHolder legoBrickPropertiesHolder){
        if(INSTANCE == null ){
            synchronized (EngineCache.class){
                if(INSTANCE == null ){
                    INSTANCE = new EngineCache(legoBrickPropertiesHolder);
                }
            }
        }
        return INSTANCE;
    }

    //Private Methods
    @SuppressWarnings(value = "unchecked")
    private <Annotation extends RoboEngine, EngineType extends LegoEngine> Map<String, EngineType>
            initCache(LegoBrickPropertiesHolder holder, Class<Annotation> annotation){
        try{
            final Map<String, LegoEngine> result = new HashMap<>();

            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage(holder.getEnginePackage()))
                    .setExecutorService(Executors.newFixedThreadPool(REFLECTION_THREADS))
                    .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner())
            );
            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(annotation);
            reflections.getConfiguration().getExecutorService().shutdown();

            for(Class c: classes){
                Annotation roboEngine = (Annotation)c.getAnnotation(annotation);
                EngineType engine = (EngineType) c.getConstructor().newInstance();
                if(Objects.isNull(engine.getEngine()) || Objects.isNull(engine.getPort())){
                    throw new EngineCacheException("CACHE ENGINE NOT VALID = " + engine);
                } else {
                    result.put(roboEngine.value(), engine);
                }
            }

            return (Map<String, EngineType>)result;
        } catch (Exception e){
            throw new EngineCacheException("ENGINE CACHE PROBLEM", e);
        }

    }

}
