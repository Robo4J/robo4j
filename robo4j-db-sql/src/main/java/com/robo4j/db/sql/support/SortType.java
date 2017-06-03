/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.db.sql.support;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum  SortType {

    //@formatter:off
    ASC         ("asc"),
    DESC        ("desc")
    ;
    //@formatter:on

    private static volatile Map<String, SortType> internMapByName;
    private final String name;

    SortType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static SortType getByName(String name) {
        if (internMapByName == null)
            internMapByName = initMapping();
        return internMapByName.entrySet().stream()
                .map(Map.Entry::getValue)
                .filter(e -> e.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private static Map<String, SortType> initMapping() {
        return Stream.of(values())
                .collect(Collectors.toMap(SortType::getName, e -> e));
    }

    @Override
    public String toString() {
        return "SortType{" +
                "name='" + name + '\'' +
                '}';
    }
}
