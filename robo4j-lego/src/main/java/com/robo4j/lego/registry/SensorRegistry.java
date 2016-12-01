/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This SensorRegistry.java  is part of robo4j.
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

package com.robo4j.lego.registry;

import com.robo4j.commons.registry.BaseRegistryProvider;
import com.robo4j.commons.registry.RoboRegistry;
import com.robo4j.commons.sensor.GenericSensor;
import com.robo4j.lego.control.LegoSensor;
import com.robo4j.lego.sensor.LegoSensorWrapper;
import com.robo4j.lego.util.LegoSensorBaseProvider;
import lejos.hardware.sensor.BaseSensor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Miro Kopecky (@miragemiko)
 * @since 28.09.2016
 */
public final class SensorRegistry implements RoboRegistry<SensorRegistry, GenericSensor> {

    private static volatile SensorRegistry INSTANCE;
    private AtomicBoolean activate;
    private Map<String, GenericSensor> sensors;
    private final BaseRegistryProvider<BaseSensor, LegoSensor> provider;

    private SensorRegistry(){
        this.sensors = new HashMap<>();
        this.activate = new AtomicBoolean(false);
        this.provider = new LegoSensorBaseProvider<>();
    }

    public static SensorRegistry getInstance(){
        if(INSTANCE == null){
            synchronized (SensorRegistry.class){
                if(INSTANCE == null){
                    INSTANCE = new SensorRegistry();
                }
            }
        }
        return INSTANCE;
    }


    @Override
    public SensorRegistry build(Map<String, GenericSensor> services) {
        services.entrySet().forEach(entry -> {
                    this.sensors.put(entry.getKey(), entry.getValue());
                }
        );
        return this;
    }

    @Override
    public GenericSensor getByName(String name) {
        return sensors.get(name);
    }

    @Override
    public Map<String, GenericSensor> getRegistry() {
        return sensors;
    }

    @Override
    public boolean activate() {
        return !(sensors == null || sensors.isEmpty()) && activateSensors();
    }

    @Override
    public boolean isActive() {
        return false;
    }

    //Private Methods
    @SuppressWarnings(value = "unchecked")
    private boolean activateSensors(){
        boolean result = true;
        sensors.entrySet()
                .forEach(e -> {
                    GenericSensor gs = e.getValue();
                    if(gs instanceof LegoSensorWrapper){
                        BaseSensor bs = provider.create((LegoSensor)gs);
                        ((LegoSensorWrapper)gs).setUnit(bs);
                    }
                });

        this.activate.set(result);
        return result;
    }
}
