/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This CommandCache.java is part of robo4j.
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

package com.robo4j.core.bridge.command.cache;

import com.robo4j.commons.annotation.BatchAnnotation;
import com.robo4j.core.control.DefaultSystemConfig;
import com.robo4j.core.lego.LegoBrickPropertiesHolder;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Created by miroslavkopecky on 17/04/16.
 */
public final class CommandCache extends CommandCacheDefault implements DefaultSystemConfig {

    private volatile static CommandCache INSTANCE;
    private volatile static Map<String, BatchCommand> cache;

    private CommandCache(LegoBrickPropertiesHolder holder){
        cache = initCache(holder, BatchAnnotation.class);
    }

    public Map<String, BatchCommand> getCache(){
        return cache;
    }

    public void addGenericCommand(BatchCommand command){
        cache.put(command.getName(), command);
    }



    public static CommandCache getInstance(LegoBrickPropertiesHolder holder){
        if(INSTANCE == null ){
            synchronized (CommandCache.class){
                if(INSTANCE == null ){
                    INSTANCE = new CommandCache(holder);
                }
            }
        }
        return INSTANCE;
    }


    //Private Methods

    /**
     * Command cache should always contain default commands
     */
    @SuppressWarnings(value = "unchecked")
    private <Annotation extends BatchAnnotation,
            Command extends BatchCommand >  Map<String, Command>
                        initCache(LegoBrickPropertiesHolder holder, Class<Annotation> anno){

        try {
            final Map<String, Command> result = (Map<String, Command>)getInitCache();

            if(validateConfiguration(holder)){
                Reflections reflections = new Reflections(new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(holder.getCommandPackage()))
                        .setExecutorService(Executors.newFixedThreadPool(REFLECTION_THREADS))
                        .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner())
                );
                Set<Class<?>> classes = reflections.getTypesAnnotatedWith(anno);
                reflections.getConfiguration().getExecutorService().shutdown();

                for(Class c: classes){
                    Annotation batchCommand = (Annotation)c.getAnnotation(anno);
                    Command instance = (Command) c.getConstructor().newInstance();
                    if(batchCommand.batch().isEmpty() && instance.getBatch().isEmpty()){
                        throw new CacheException("NO VALID DATA instance= " + c.getSimpleName());
                    } else if(!batchCommand.batch().isEmpty()){
                        instance.setBatch(batchCommand.batch());
                    }

                    result.put(batchCommand.name(), instance);

                }
            }

            return result;
        } catch (Exception e){
            throw new CacheException("COMMAND CACHE PROBLEM", e);
        }
    }

    //Private Methods
    private boolean validateConfiguration(LegoBrickPropertiesHolder holder){
        return Objects.nonNull(holder.getCommandPackage()) && !holder.getCommandPackage().isEmpty();
    }


}
