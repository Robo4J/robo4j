/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This RoboClassLoader.java is part of robo4j.
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
 * Singleton instance of classLoader
 *
 * Created by miroslavkopecky on 23/05/16.
 */
public final class RoboClassLoader {

    private static volatile RoboClassLoader INSTANCE;
    private volatile ClassLoader cl;

    private RoboClassLoader(){
        cl =  Thread.currentThread().getContextClassLoader();
    }

    public static RoboClassLoader getInstance(){
        if(INSTANCE == null){
            synchronized (RoboClassLoader.class){
                if(INSTANCE == null){
                    INSTANCE = new RoboClassLoader();
                }
            }
        }
        return INSTANCE;
    }

    //Public Methods
    public ClassLoader getClassLoader(){
        return cl;
    }

    public InputStream getResource(final String name){
        return cl.getResourceAsStream(name);
    }

}
