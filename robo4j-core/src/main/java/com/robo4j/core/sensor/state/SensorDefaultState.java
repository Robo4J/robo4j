/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This SensorDefaultState.java is part of robo4j.
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

import com.robo4j.core.sensor.SensorType;

/**
 * Created by miroslavkopecky on 22/05/15.
 */
public class SensorDefaultState implements SensorState  {

    private final SensorType type;
    private final Long timestamp;
    private final String value;
    private final int priority;


    public SensorDefaultState(SensorType type, Long timestamp, String value, Integer priority) {
        this.type = type;
        this.timestamp = timestamp;
        this.value = value;
        this.priority = priority;
    }

    public SensorType getType() {
        return type;
    }

    @Override
    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getNumberValue() {
        return value;
    }

    @Override
    public String getTestValue() {
        return value;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(SensorDefaultState o) {
        return (this.priority > o.getPriority()) ? 1 : (this.priority < o.getPriority()) ? -1 : 0;
    }

    @Override
    public String toString() {
        return "SensorDefaultState{" +
                "type=" + type +
                ", timestamp=" + timestamp +
                ", value='" + value + '\'' +
                '}';
    }
}
