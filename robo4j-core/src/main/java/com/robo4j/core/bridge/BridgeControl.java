/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This BridgeControl.java is part of robo4j.
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

import com.robo4j.commons.agent.GenericAgent;
import com.robo4j.commons.annotation.SystemConfig;
import com.robo4j.commons.annotation.SystemProperties;
import com.robo4j.commons.annotation.SystemProvider;
import com.robo4j.commons.control.RoboSystemConfig;
import com.robo4j.core.bridge.command.cache.CacheException;
import com.robo4j.core.control.ControlCommandsAdapter;
import com.robo4j.core.control.ControlSystem;
import com.robo4j.core.control.ControlUtil;
import com.robo4j.core.control.RoboSystemProperties;
import com.robo4j.core.io.NetworkUtils;
import com.robo4j.core.lego.LegoBrickProperties;
import com.robo4j.core.lego.LegoBrickPropertiesHolder;
import com.robo4j.core.lego.LegoBrickRemoteProvider;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Miro Kopecky (@miragemiko)
 * @since 21.04.2016
 */
@SuppressWarnings(value = "unchecked")
public abstract class BridgeControl<Configuration extends RoboSystemConfig> extends AbstractControlBridge implements ControlSystem<Configuration> {

    private static final Logger logger = LoggerFactory.getLogger(BridgeControl.class);
    private static final int START = 0;

    protected volatile ControlCommandsAdapter controlCommandsAdapter;
    private final String corePackage;
    protected Map<String, Configuration> systemCache;
    protected final List<GenericAgent> genericAgents;

    protected BridgeControl(final String corePackage){
        super();
        this.corePackage = corePackage;
        systemCache = initCache(SystemProperties.class, SystemConfig.class, SystemProvider.class);
        genericAgents = new LinkedList<>();
    }

    @Override
    public Map<String, Configuration> getSystemCache(){
        return initCache();
    }


    private Map<String, Configuration> initCache(Class<? extends Annotation>... annos){

        final Map<String, Configuration> result = new ConcurrentHashMap<>();
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(corePackage))
                .setExecutorService(Executors.newFixedThreadPool(REFLECTION_THREADS))
                .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner())
        );

        AtomicInteger configNumber = new AtomicInteger(START);
        Arrays.asList(annos).stream()
                .forEach(anno ->{
                    try {
                        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(anno);

//                            if(classes.size() == NOT_ALLOWED ){
//                                throw new CacheException("NOT FULL CONFIGURATION!");
//                            }
//
//                            if(MAX_ALLOWED != classes.size() ){
//                                throw new CacheException("SYSTEM CONFIGURATION NOT ALLOWED!");
//                            }

                        for(Class c: classes){
                            Annotation typeAnnotation = c.getAnnotation(anno);

                            Method instanceMethod = null;
                            RoboSystemConfig instance = null;
                            String key = null;

                                /* System Properites are provided by annotation */
                            if(typeAnnotation instanceof SystemProperties && containsInterface(c, LegoBrickProperties.class)) {
                                key = ControlUtil.SYSTEM_CONFIG;
                                instanceMethod = c.getDeclaredMethod(METHOD_PROPERTIES_BRICKS);
                                LegoBrickProperties legoBrickProperties = (LegoBrickProperties) c.newInstance();
                                instance = new LegoBrickPropertiesHolder(legoBrickProperties);
                                logger.info("SystemProperties = INSTANCE = " + legoBrickProperties);
                                configNumber.incrementAndGet();
                            }

                            if(typeAnnotation instanceof SystemConfig && !result.containsKey(ControlUtil.SYSTEM_CONFIG)){
                                key = ControlUtil.SYSTEM_CONFIG;
                                instanceMethod = c.getDeclaredMethod(METHOD_CONFIG);
                                instance = (RoboSystemConfig) instanceMethod.invoke(null);
                                configNumber.incrementAndGet();
                            }

                            if(typeAnnotation instanceof SystemProvider){
                                key = ControlUtil.SYSTEM_PROVIDER;
                                RoboSystemProperties properties = (RoboSystemProperties) result.get(ControlUtil.SYSTEM_CONFIG);

                                logger.info("SYSTEM PROVIDER = " + properties);

                                instanceMethod = c.getDeclaredMethod(METHOD_PROVIDER, String.class);
                                instance = (RoboSystemConfig) instanceMethod.invoke(null,
                                        properties.getProperties().getAddress());
                            }


                            if(key != null && instance != null ){
                                result.put(key, (Configuration) instance);
                            }
                        }
                    } catch (Exception e){
                        throw new CacheException("SYSTEM CACHE PROBLEM", e);
                    }

                });
        /* there is not available brick provider */

        reflections.getConfiguration().getExecutorService().shutdown();


        return configurationCheck(result, configNumber) ? result : wrongConfig(result);

    }

    //Private Methods
    private boolean containsInterface(Class<?> clazz, Class<?> implementedInterface){
        return Arrays.asList(clazz.getInterfaces()).stream()
                .filter(c -> c.getSimpleName().equals(implementedInterface.getSimpleName()))
                .count() == 1;
    }

    private boolean configurationCheck(final Map<String, Configuration> result, AtomicInteger configNumber){
        if(configNumber.get() > 0 && !result.containsKey(ControlUtil.SYSTEM_PROVIDER)){
            final RoboSystemProperties properties = (RoboSystemProperties) result.get(ControlUtil.SYSTEM_CONFIG);
            final String ipAddress = properties.getProperties().getAddress();

            if(NetworkUtils.pingBrick(ipAddress).get()){
                String key = ControlUtil.SYSTEM_PROVIDER;
                RoboSystemConfig instance =  LegoBrickRemoteProvider.getInstance(ipAddress);
                result.put(key, (Configuration) instance);
                logger.warn("DEFAULT SYSTEM PROVIDER HAS BEEN USED");
            } else {
                logger.error("DEFAULT SYSTEM IS NOT AVAILABLE -> NETWORK= " + ipAddress);
                return false;
            }

        }

        return result.entrySet().stream()
                .map(Map.Entry::getKey)
                .filter(k -> k.equals(ControlUtil.SYSTEM_PROVIDER) || k.equals(ControlUtil.SYSTEM_CONFIG))
                .count() == REQUIRED_CONFIGURATION;
    }

    private Map<String, Configuration> wrongConfig(Map<String, Configuration> result){
        logger.warn("Not possible configuration");
        return result;
    }
}
