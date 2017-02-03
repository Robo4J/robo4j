/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This BrickUtils.java  is part of robo4j.
 * module: robo4j-hw-lego
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.hw.lego.util;

/**
 * Some useful Lego Mindstorm brick utils
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 01.02.2017
 */
public class BrickUtils {

    /* identifies Lego Brick Mindstorm buttons */
    public static final String PREFIX_BUTTON = "button_";

    /* identifies test resources */
    private static final String PREFIX_TEST = "test_";

    public static String getButton(String name){
        return PREFIX_BUTTON.concat(name).toLowerCase();
    }

    public static String getTestResource(String name){
        return PREFIX_TEST.concat(name).toLowerCase();
    }
}
