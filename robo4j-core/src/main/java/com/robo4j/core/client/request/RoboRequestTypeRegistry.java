/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This RoboRequestTypeProvider.java  is part of robo4j.
 * module: robo4j-core
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

package com.robo4j.core.client.request;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Provider provides pre-defined types of mapping
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
//TODO fix the concurrency 
public final class RoboRequestTypeRegistry {

    private static volatile RoboRequestTypeRegistry INSTANCE;
    private final Map<String, Set<RoboRequestElement>> pathValues = new HashMap<>();

    public RoboRequestTypeRegistry() {
    }

    public static RoboRequestTypeRegistry getInstance() {
        if (INSTANCE == null) {
            synchronized (RoboRequestTypeRegistry.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RoboRequestTypeRegistry();
                }
            }
        }
        return INSTANCE;
    }

    public void addPathWithValues(String path, Set<RoboRequestElement> values){
        pathValues.put(path, values);
    }

    public Set<RoboRequestElement> getPathValues(String path){
        return pathValues.get(path);
    }

    @Override
    public String toString() {
        return "RoboRequestTypeProvider{" +
                "pathValues=" + pathValues +
                '}';
    }
}
