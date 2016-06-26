/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LegoBrickSetupProperties.java is part of robo4j.
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

package com.robo4j.line.system;

import com.robo4j.commons.annotation.SystemProperties;
import com.robo4j.commons.control.RoboSystemConfig;
import com.robo4j.core.lego.LegoBrickProperties;
import com.robo4j.core.lego.LegoBrickPropertiesHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * NOTE: required to change BRICK_IP_1 !!!
 *
 * Created by miroslavkopecky on 04/06/16.
 */

@SystemProperties
public class LegoBrickSetupProperties implements LegoBrickProperties, RoboSystemConfig {

    private Map<String, String> bricks;
    private String commandPackage;
    private String enginePackage;


    public LegoBrickSetupProperties() {
        final Map<String, String> bricks = new HashMap<>();
        /* required to change BRICK_IP_1  */
        bricks.put(LegoBrickPropertiesHolder.BRICK_IP_1, "192.168.178.26");
        this.bricks = bricks;
        this.commandPackage = "com.robo4j.line.commands";
        this.enginePackage = "com.robo4j.line.engine";
    }

    @Override
    public Map<String, String> getBricks() {
        return bricks;
    }

    @Override
    public String getCorePackage() {
        return null;
    }

    @Override
    public String getCommandPackage() {
        return commandPackage;
    }

    @Override
    public String getEnginePackage() {
        return enginePackage;
    }

    @Override
    public String getSensorPackage() {
        return null;
    }

    @Override
    public String toString() {
        return "LegoBrickPropertiesTest{" +
                "bricks=" + bricks +
                ", commandPackage='" + commandPackage + '\'' +
                '}';
    }
}
