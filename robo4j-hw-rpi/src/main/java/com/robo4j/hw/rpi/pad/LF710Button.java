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
package com.robo4j.hw.rpi.pad;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Logitech F710 Gamepad possible buttons
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum LF710Button implements LF710Input {

    //@formatter:off
    BLUE                ((short)0,  "blue"),
    GREEN               ((short)1,  "green"),
    RED                 ((short)2,  "red"),
    YELLOW              ((short)3,  "yellow"),
    FRONT_UP_LEFT       ((short)4,  "front_up_left"),
    FRONT_UP_RIGHT      ((short)5,  "front_up_right"),
    FRONT_DOWN_LEFT     ((short)6,  "front_down_left"),
    FRONT_DOWN_RIGHT    ((short)7,  "front_down_right"),
    BACK                ((short)8,  "back"),
    START               ((short)9,  "start"),
    JOYSTICK_LEFT       ((short)10, "joystick left"),
    JOYSTICK_RIGHT      ((short)11, "joystick right"),
    UNKNOWN((short)-1, "")
    ;
    //@formatter:on

    private static final Map<Short, LF710Button> internMapByMask = initMapping();
    private final short mask;
    private final String name;

    LF710Button(short mask, String name) {
        this.mask = mask;
        this.name = name;
    }

    public short getMask() {
        return mask;
    }

    public String getDesc() {
        return name;
    }

    private static Map<Short, LF710Button> initMapping() {
        return Stream.of(values()).collect(Collectors.toMap(LF710Button::getMask, e -> e));
    }

    public static LF710Button getByMask(Short mask) {
        return internMapByMask.values().stream()
                .filter(e -> e.getMask() == mask).findFirst()
                .orElse(UNKNOWN);
    }

    @Override
    public String toString() {
        return "LF710Button{" + "mask=" + mask + ", name='" + name + '\'' + '}';
    }
}
