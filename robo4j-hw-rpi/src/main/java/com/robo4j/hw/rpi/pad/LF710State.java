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
 *
 * simplified state of the Logitech F710 input element
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum LF710State {

    //@formatter:off
    RELEASED    ((short)0, "released"),
    PRESSED     ((short)1, "pressed");
    //@formatter:on

    private static volatile Map<Short, LF710State> internMapByMask;
    private final short mask;
    private final String name;

    LF710State(short mask, String name) {
        this.mask = mask;
        this.name = name;
    }

    public short getMask() {
        return mask;
    }

    public String getName() {
        return name;
    }

    private static Map<Short, LF710State> initMapping() {
        return Stream.of(values()).collect(Collectors.toMap(LF710State::getMask, e -> e));
    }

    public static LF710State getByMask(Short mask) {
        if (internMapByMask == null)
            internMapByMask = initMapping();
        return internMapByMask.entrySet().stream().map(Map.Entry::getValue).filter(e -> e.getMask() == mask).findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return "LF710State{" + "mask=" + mask + ", name='" + name + '\'' + '}';
    }
}
