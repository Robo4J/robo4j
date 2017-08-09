/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This LegoSensorMessage.java  is part of robo4j.
 * module: robo4j-units-lego
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.units.lego.sensor;

import com.robo4j.core.RoboReference;
import com.robo4j.hw.lego.enums.SensorTypeEnum;

/**
 * Lego Sensor Message is generic for all available sesnors
 * @see {@link SensorTypeEnum}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class LegoSensorMessage {

    private final RoboReference<?> source;
    private final SensorTypeEnum type;
    private final String value;

    public LegoSensorMessage(SensorTypeEnum type, String value) {
        this(null, type, value);
    }

    public LegoSensorMessage(RoboReference<?> source, SensorTypeEnum type, String value) {
        this.source = source;
        this.type = type;
        this.value = value;
    }

    public RoboReference<?> getSource() {
        return source;
    }

    public SensorTypeEnum getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#toString()
         */
    @Override
    public String toString() {
        return String.format("Type: %s, Value: %s, Source: %s", type, value, String.valueOf(source));
    }
}
