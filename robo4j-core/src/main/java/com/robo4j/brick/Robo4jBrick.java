/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This Robo4jBrick.java  is part of robo4j.
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

package com.robo4j.brick;

import com.robo4j.brick.manager.RegistryManager;
import com.robo4j.brick.reflect.AbstractClient;
import com.robo4j.brick.reflect.RoboReflectionScan;
import com.robo4j.commons.control.RoboSystemConfig;
import com.robo4j.commons.enums.RegistryTypeEnum;
import com.robo4j.commons.registry.RoboRegistry;
import com.robo4j.lego.registry.EngineRegistry;
import com.robo4j.lego.registry.SensorRegistry;

/**
 * Main class needs to be initiated
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 30.11.2016
 */
public class Robo4jBrick extends AbstractClient {

    private boolean initiate;
    public Robo4jBrick(Class<?> clazz, boolean test) {
        super(new RoboReflectionScan(clazz).init(test));
        this.initiate = false;
    }

    public boolean init(){
        this.initiate = active.get();
        return initiate;
    }

    public RegistryManager getRegistry(){
        return  RegistryManager.getInstance();
    }

    @SuppressWarnings(value = "unchecked")
    public RoboRegistry<RoboRegistry, RoboSystemConfig> getRegistryByType(RegistryTypeEnum type){
        return RegistryManager.getInstance().getRegistryByType(type);
    }

    public boolean activateEngineRegistry(){
        return EngineRegistry.getInstance().activate();
    }

    public boolean activateSensorRegistry(){
        return SensorRegistry.getInstance().activate();
    }
}
