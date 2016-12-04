/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This RegistryManager.java  is part of robo4j.
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

package com.robo4j.commons.registry;

import com.robo4j.commons.control.RoboSystemConfig;
import com.robo4j.commons.enums.RegistryTypeEnum;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 *
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 27.11.2016
 */
public final class RegistryManager<RegistryType extends RoboRegistry, ConfigType extends RoboSystemConfig> {
    private static final List<RegistryTypeEnum> list = Arrays.asList(RegistryTypeEnum.values());
    private static volatile RegistryManager INSTANCE;
    private AtomicBoolean active;
    private volatile Map<RegistryTypeEnum, RoboRegistry> registries;

    private RegistryManager(){
        this.registries = new HashMap<>();
        this.active = new AtomicBoolean(false);
    }

    public static RegistryManager getInstance(){
        if(INSTANCE == null){
            synchronized (RegistryManager.class){
                if(INSTANCE == null){
                    INSTANCE = new RegistryManager();
                }
            }
        }
        return INSTANCE;
    }

    public RegistryManager addAll(Map<RegistryTypeEnum, RoboRegistry> map){
        registries.putAll(map);
        return this;
    }
    public RoboRegistry add(RegistryTypeEnum type, RoboRegistry registry){
        return registries.put(type, registry);
    }

    public boolean isActive(){
        if(!active.get()){
            active.set(registries.entrySet().stream()
                    .map(Map.Entry::getKey)
                    .filter(list::contains)
                    .count() == list.size());
        }
        return active.get();
    }

    @SuppressWarnings(value = "unchecked")
    public  RegistryType initRegistry(RegistryTypeEnum name, Map<RegistryTypeEnum, ConfigType> types){
        return (RegistryType)registries.get(name).build(types);
    }

    public RoboSystemConfig getItemByRegistry(RegistryTypeEnum registry, String name){
        return registries.get(registry).getByName(name);
    }

    @SuppressWarnings(value = "unchecked")
    public RoboRegistry<RoboRegistry, RoboSystemConfig> getRegistryByType(RegistryTypeEnum type){
        return registries.get(type);
    }

    public List<String> getRegistryNames(){
        return registries.entrySet().stream()
                .map(Map.Entry::getKey)
                .map(RegistryTypeEnum::getName)
                .collect(Collectors.toList());
    }
}
