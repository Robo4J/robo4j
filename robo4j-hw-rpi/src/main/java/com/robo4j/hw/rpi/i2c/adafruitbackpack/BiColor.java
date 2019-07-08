/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.hw.rpi.i2c.adafruitbackpack;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum  BiColor {

    //@formatter:off
    OFF     (0),
    RED     (1),
    YELLOW  (2),
    GREEN   (3)
    ;
    //@formatter:on

    private final int value;

    BiColor(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static BiColor getByValue(int code) {
        for (BiColor r : values()) {
            if (code == r.value) {
                return r;
            }
        }
        return OFF;
    }
}
