/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This ProviderRegistry.java  is part of robo4j.
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Created by miroslavkopecky on 03/12/2016.
 */
public final class ProviderRegistry implements RoboRegistry<ProviderRegistry, BaseRegistryProvider> {

    private static volatile ProviderRegistry INSTANCE;
    private volatile Map<String, BaseRegistryProvider> providers;
    private AtomicBoolean active;

    private ProviderRegistry(){
        this.providers = new HashMap<>();
        this.active = new AtomicBoolean(false);
    }

    public static ProviderRegistry getInstance(){
        if(INSTANCE == null){
            synchronized (UnitRegistry.class){
                if(INSTANCE == null){
                    INSTANCE = new ProviderRegistry();
                }
            }
        }

        return INSTANCE;
    }


    @Override
    public ProviderRegistry build(Map<String, BaseRegistryProvider> providers) {
        this.providers = providers.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        this.active.set(true);
        return this;
    }

    @Override
    public BaseRegistryProvider getByName(String name) {
        return this.providers.get(name);
    }

    @Override
    public Map<String, BaseRegistryProvider> getRegistry(){
        return this.providers;
    }

    @Override
    public boolean activate() {
        return false;
    }

    @Override
    public boolean isActive() {
        return active.get();
    }
}
