/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.hw.rpi.utils.I2cBus;

import java.io.IOException;

/**
 * LedBackpackFactory create an Backpack device by defined type
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class LedBackpackFactory {

    public static AbstractBackpack createDevice(I2cBus bus, int address, LedBackpackType type, int brightness)
            throws IOException {
        return switch (type) {
            case BI_COLOR_BAR_24 -> new BiColor24BarDevice(bus, address, brightness);
            case BI_COLOR_MATRIX_8x8 -> new BiColor8x8MatrixDevice(bus, address, brightness);
            case ALPHANUMERIC -> new AlphanumericDevice(bus, address, brightness);
        };
    }
}
