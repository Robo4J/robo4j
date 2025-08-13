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

package com.robo4j.hw.rpi.utils;

import java.util.stream.Stream;

/**
 * GpioPin addresses
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum GpioPin {
    GPIO_UNKNOWN(-1),
    GPIO_00(0),
    GPIO_01(1),
    GPIO_02(2),
    GPIO_03(3),
    GPIO_04(4),
    GPIO_05(5),
    GPIO_06(6),
    GPIO_07(7),
    GPIO_08(8),
    GPIO_09(9),
    GPIO_10(10),
    GPIO_11(11),
    GPIO_12(12),
    GPIO_13(13),
    GPIO_14(14),
    GPIO_15(15),
    GPIO_16(16),
    GPIO_17(17),
    GPIO_18(18),
    GPIO_19(19),
    GPIO_20(20),
    GPIO_21(21),
    GPIO_22(22),
    GPIO_23(23),
    GPIO_24(24),
    GPIO_25(25),
    GPIO_26(26),
    GPIO_27(27),
    GPIO_28(28),
    GPIO_29(29),
    GPIO_30(30),
    GPIO_31(31);


    private final int address;

    GpioPin(int address) {
        this.address = address;
    }

    public int address() {
        return address;
    }

    public static GpioPin getByAddress(int address) {
        return Stream.of(values()).filter(p -> p.address == address).findFirst().orElse(GPIO_UNKNOWN);
    }

    // TODO : check for empty string "" constant
    public static GpioPin getByName(String name) {
        var nameLowerCase = name == null ? "" : name.toLowerCase();
        return Stream.of(values()).filter(p -> p.name().toLowerCase().equals(nameLowerCase)).findFirst().orElse(GPIO_UNKNOWN);
    }
}

