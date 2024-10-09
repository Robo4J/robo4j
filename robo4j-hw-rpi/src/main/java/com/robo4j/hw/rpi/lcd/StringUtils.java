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
package com.robo4j.hw.rpi.lcd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some simple string utils.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class StringUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringUtils.class);

    public static String rightFormat(String string, int length) {
        return String.format("%" + length + "s", string);
    }

    public static String centerFormat(String string, int length) {
        int diff = length - string.length();
        return String.format("%-" + length + "s", getSpaces(diff / 2) + string);
    }

    private static String getSpaces(int total) {
        String tmp = "";
        for (int i = 0; i < total; i++) {
            tmp += " ";
        }
        return tmp;
    }

    public static void main(String... bla) {
        LOGGER.info(centerFormat("Center me", 20));
    }
}
