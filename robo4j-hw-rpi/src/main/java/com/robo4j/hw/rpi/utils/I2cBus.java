/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.hw.rpi.utils;

import java.util.stream.Stream;

public enum I2cBus {
    BUS_UNKNOWN(-1),
    BUS_0(0),
    BUS_1(1)
    ;

    private final int address;
    I2cBus(int address) {
        this.address = address;
    }

    public int address(){
        return address;
    }

    public I2cBus getByAddress(int address) {
        return Stream.of(values()).filter(p -> p.address == address).findFirst().orElse(BUS_UNKNOWN);
    }
}
