/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LegoBrickPropertiesHolder.java is part of robo4j.
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

package com.robo4j.core.lego;

import com.robo4j.commons.control.RoboSystemConfig;
import com.robo4j.core.control.RoboSystemProperties;

import java.util.Map;

/**
 * @author Miro Kopecky (@miragemiko)
 * @since 11.04.2016
 */
public class LegoBrickPropertiesHolder implements RoboSystemProperties, RoboSystemConfig {

    public static final String BRICK_IP_1 = "brickip1";
    public static final String CORE_PACKAGE = "core_package";
    public static final String COMMAND_PACKAGE = "commands_package";
    public static final String ENGINE_PACKAGE = "engine_package";
    public static final String SENSOR_PACKAGE = "sensor_package";
    public static final String UNIT_PACKAGE = "unit_package";
    private Map<String, String> map;

    public LegoBrickPropertiesHolder(final Map<String, String> map) {
        this.map = map;
    }

    public LegoBrickPropertiesHolder(final LegoBrickProperties legoBrickProperties){
        /* add all available lego bricks */
        this.map = legoBrickProperties.getBricks();
        //TODO: corepackage is currently duplicate to ControlPad
        map.put(CORE_PACKAGE, legoBrickProperties.getCorePackage());
        map.put(COMMAND_PACKAGE, legoBrickProperties.getCommandPackage());
        map.put(ENGINE_PACKAGE, legoBrickProperties.getEnginePackage());
        map.put(SENSOR_PACKAGE, legoBrickProperties.getSensorPackage());
        map.put(UNIT_PACKAGE, legoBrickProperties.getUnitPackage());
    }

    @Override
    public LegoBrickPropertiesHolder getProperties() {
        return this;
    }

    public String getAddress() {
        return map.get(BRICK_IP_1);
    }

    public void setAddress(String address) {
        this.map.replace(BRICK_IP_1, address);
    }

    public String getCorePackage(){
        return map.get(CORE_PACKAGE);
    }

    public String getCommandPackage(){
        return map.get(COMMAND_PACKAGE);
    }

    public String getEnginePackage(){
        return map.get(ENGINE_PACKAGE);
    }

    public String getSensorPackage(){
        return map.get(SENSOR_PACKAGE);
    }

    public String getUnitPackage(){
        return map.get(UNIT_PACKAGE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LegoBrickPropertiesHolder)) return false;

        LegoBrickPropertiesHolder that = (LegoBrickPropertiesHolder) o;

        return map != null ? map.equals(that.map) : that.map == null;

    }

    @Override
    public int hashCode() {
        return map != null ? map.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "LegoBrickProperties{" +
                "map='" + map + '\'' +
                '}';
    }
}
