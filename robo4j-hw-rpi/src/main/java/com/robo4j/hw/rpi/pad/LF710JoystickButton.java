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
package com.robo4j.hw.rpi.pad;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Logitech F710 Joystick related elements
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum LF710JoystickButton implements LF710Input {

    //@formatter:off
    LEFT_X      ((short)0, "left x"),
    LEFT_Y      ((short)1, "left y"),
    RIGHT_X     ((short)2, "right x"),
    RIGHT_Y     ((short)3, "right y"),
    PAD_X       ((short)4, "pad x"),
    PAD_Y       ((short)5, "pad y"),
    UNKNOWN     ((short)-1, "")
    ;
    //@formatter:on

    private static final Map<Short, LF710JoystickButton> internMapByMask = initMapping();
    private final short mask;
    private final String desc;

    LF710JoystickButton(short mask, String desc) {
        this.mask = mask;
        this.desc = desc;
    }

    public short getMask() {
        return mask;
    }

    public String getDesc() {
        return this.desc;
    }


    private static Map<Short, LF710JoystickButton> initMapping() {
        return Stream.of(values()).collect(Collectors.toMap(LF710JoystickButton::getMask, e -> e));
    }

    public static LF710JoystickButton getByMask(Short mask) {
        return internMapByMask.values().stream()
                .filter(e -> e.getMask() == mask).findFirst()
                .orElse(UNKNOWN);
    }

    @Override
    public String toString() {
        return "LF710JoystickButton{" + "mask=" + mask + ", name='" + desc + '\'' + '}';
    }
}
