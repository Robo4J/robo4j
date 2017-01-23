/*
 * Copyright (C) 2017. Miroslav Wengner, Marcus Hirt
 * This UnitProducerRegistry.java  is part of robo4j.
 * module: robo4j-commons
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.robo4j.core.unit.UnitProducer;

/**
 * @author Miro Wengner (@miragemiko)
 * @since 18.01.2017
 */
public class UnitProducerRegistry implements RoboRegistry<UnitProducerRegistry, UnitProducer>  {

    private static volatile UnitProducerRegistry INSTANCE;
    private volatile Map<String, UnitProducer> producers;
    private AtomicBoolean active;

    private UnitProducerRegistry() {
        this.producers = new HashMap<>();
        this.active = new AtomicBoolean(false);
    }

    public static UnitProducerRegistry getInstance() {
        if (INSTANCE == null) {
            synchronized (UnitProducerRegistry.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UnitProducerRegistry();
                }
            }
        }

        return INSTANCE;
    }

    @Override
    public UnitProducerRegistry build(Map<String, UnitProducer> service) {
        service.entrySet().forEach(entry -> {
            this.producers.put(entry.getKey(), entry.getValue());
        });
        this.active.set(true);
        return this;
    }

    @Override
    public UnitProducer getByName(String name) {
        return this.producers.get(name);
    }

    @Override
    public Map<String, UnitProducer> getRegistry() {
        return this.producers;
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
