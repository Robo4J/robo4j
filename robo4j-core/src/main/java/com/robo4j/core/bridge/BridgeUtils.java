/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This BridgeUtils.java is part of robo4j.
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

import java.util.regex.Pattern;

/**
 * Created by miroslavkopecky on 25/04/16.
 */
public final class BridgeUtils {

    protected static final String BUS_GUARDIAN_BUS = "guardianBus";
    protected static final String BUS_CORE_BUS = "coreBus";
    protected static final String BUS_SENSOR_BUS = "sensorBus";

    /* used to pars the commands */
    public static final Pattern commandLinePattern = Pattern.compile("(^[a-z]{4,5})\\(([-]?[0-9]+)\\)");
    public static final String BUS_COMMAND_CONSUMER = "commandConsumerBus";

}

