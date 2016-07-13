/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This RoboClassUtils.java is part of robo4j.
 *
 *     robo4j is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     robo4j is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.robo4j.core.util;

import java.io.InputStream;

/**
 *
 * Class related utils
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 23.05.2016
 */
public class RoboClassUtils {

    /**
     * Return current Robo4j ClassLoader
     * preparetion for multiple classloader usage
     * @return System ClassLoader
     */
    public static ClassLoader getClassLoader(){
        return RoboClassLoader.getInstance().getClassLoader();
    }

    public static InputStream getResource(String name){
        return RoboClassLoader.getInstance().getResource(name);
    }

}
