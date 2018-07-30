/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This LegoUtils.java  is part of robo4j.
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

package com.robo4j.units.lego.utils;

import com.robo4j.util.StringConstants;

/**
 * Some useful utils for lego units
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class LegoUtils {

    public static final int DEFAULT_1 = 1;
    public static final int DEFAULT_0 = 0;
    public static final int PLATFORM_THREAD_POOL_SIZE = 2;
    public static final int SINGLE_THREAD_POOL_SIZE = 1;
    public static final int TERMINATION_TIMEOUT = 2;
    public static final int KEEP_ALIVE_TIME = 10;
    public static final String VALUE_SEPARATOR = ",";
    public  static final String VALUE_INFINITY = "Infinity";


    public static String parseOneElementString(String value){
        return value.replace(VALUE_SEPARATOR, StringConstants.EMPTY);
    }

    public static float parseFloatStringWithInfinityDefault(String value, float maximum){
        return VALUE_INFINITY.equals(value) ? maximum : Float.parseFloat(value);
    }

}
