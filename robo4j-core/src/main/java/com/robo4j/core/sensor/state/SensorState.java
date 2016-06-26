/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This SensorState.java is part of robo4j.
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

package com.robo4j.core.sensor.state;

import com.robo4j.commons.concurrent.TransferSignal;
import com.robo4j.lego.enums.LegoSensorEnum;

/**
 * @author Miro Kopecky (@miragemiko)
 * @since 22.05.2016
 */
public interface SensorState extends TransferSignal, Comparable<SensorDefaultState> {

    Long getTimestamp();
    LegoSensorEnum getType();
    String getNumberValue();
    /**
     * used for testing purposes
     * @return - String
     */
    String getTestValue();

    int getPriority();
}
