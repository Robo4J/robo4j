/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LoadProperties.java is part of robo4j.
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

package com.robo4j.core.lego;

import com.robo4j.core.annotation.SystemConfig;
import com.robo4j.core.control.RoboSystemConfig;
import com.robo4j.core.control.RoboSystemProperties;
import com.robo4j.core.properties.LoadPropertiesHelper;


/**
 * Created by miroslavkopecky on 11/04/16.
 */
@SystemConfig
public class LoadProperties implements RoboSystemConfig, RoboSystemProperties {
    private static final String ENDING = ".properties";
    private static final String BRICK_PROPERTIES="brick";


    private volatile static LoadProperties INSTANCE;
    private volatile LegoBrickPropertiesHolder properties;

    private LoadProperties(){
        properties = loadProperties();
    }

    public static LoadProperties load(){
        if(INSTANCE == null ){
            synchronized (LegoBrickPropertiesHolder.class){
                if(INSTANCE == null ){
                    INSTANCE = new LoadProperties();
                }
            }
        }
        return INSTANCE;

    }

    //Public Method
    @Override
    public LegoBrickPropertiesHolder getProperties(){
        return properties;
    }


    //Private Method
    private LegoBrickPropertiesHolder loadProperties(){
        return LoadPropertiesHelper.loadProperties(BRICK_PROPERTIES + ENDING);
    }

}
