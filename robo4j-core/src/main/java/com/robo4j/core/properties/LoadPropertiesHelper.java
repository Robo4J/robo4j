/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LoadPropertiesHelper.java is part of robo4j.
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

package com.robo4j.core.properties;

import com.robo4j.core.lego.LegoBrickPropertiesHolder;
import com.robo4j.core.util.RoboClassUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by miroslavkopecky on 23/04/16.
 */
public final class LoadPropertiesHelper {
    private static final String DELIMINATOR = "=";
    private static final int POS_KEY = 0;
    private static final int POS_VALUE = 1;

    public static LegoBrickPropertiesHolder loadProperties(String propertyFile){
        try(InputStream inputStream = RoboClassUtils.getResource(propertyFile);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader)){

            final Map<String, String> map = bufferedReader.lines().map(e -> {
                String[] array = e.split(DELIMINATOR);
                return new Map.Entry<String, String>(){
                    @Override
                    public String getKey() {
                        return array[POS_KEY];
                    }

                    @Override
                    public String getValue() {
                        return array[POS_VALUE];
                    }

                    @Override
                    public String setValue(String value) {
                        return value;
                    }
                };
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return new LegoBrickPropertiesHolder(map);

        } catch (Exception e ){
            throw new PropertiesException("Properties: Something doesn't work", e);
        }
    }
}
