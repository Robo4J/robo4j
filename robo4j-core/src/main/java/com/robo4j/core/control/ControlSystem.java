/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This ControlSystem.java is part of robo4j.
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

package com.robo4j.core.control;

import java.util.Map;

/**
 * Created by miroslavkopecky on 23/04/16.
 */
public interface ControlSystem <Configuration extends RoboSystemConfig> extends DefaultSystemConfig {

    String PACKAGE_CORE = "com.robo4j.core";
    String METHOD_CONFIG = "load";
    String METHOD_PROVIDER = "getInstance";
    String METHOD_PROPERTIES_BRICKS = "getBricks";
    String METHOD_PROPERTIES_CORE_PACKAGE = "getCorePackage";
    String METHOD_PROPERTIES_COMMAND_PACKAGE = "getCommandPackage";
    int REQURED_CONFIGURATION = 2;
    Map<String, Configuration> getSystemCache();

}
