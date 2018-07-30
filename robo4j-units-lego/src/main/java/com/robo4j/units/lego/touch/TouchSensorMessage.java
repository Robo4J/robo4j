/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.units.lego.touch;

import java.util.stream.Stream;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum TouchSensorMessage {

    NONE    (0, ""),
    RELEASED(1, "0.0"),
    PRESSED (2, "1.0");

    private final int id;
    private final String value;

    TouchSensorMessage(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public static TouchSensorMessage parseValue(String value) {
        return Stream.of(values()).filter(v -> v.getValue().equals(value)).findFirst().orElse(NONE);
    }

    @Override
    public String toString() {
        return "TouchSensorMessage{" +
                "id=" + id +
                ", value='" + value + '\'' +
                '}';
    }
}
