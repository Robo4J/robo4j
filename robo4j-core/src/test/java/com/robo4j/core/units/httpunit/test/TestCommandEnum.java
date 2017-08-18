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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.units.httpunit.test;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum TestCommandEnum {

    //@formatter:off
    MOVE        (1, "move"),
    BACK        (2, "back");
    ;
    //@formatter:on

    private static volatile Map<Integer, TestCommandEnum> nameToEnum;
    private int id;
    private String name;

    TestCommandEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    private static Map<Integer, TestCommandEnum> initMapping() {
        return Stream.of(values()).collect(Collectors.toMap(TestCommandEnum::getId, e -> e));
    }

    public static TestCommandEnum getByName(String name){
        if(nameToEnum == null){
            nameToEnum = initMapping();
        }
        //@formatter:off
        return nameToEnum.entrySet().stream()
                .filter(e -> e.getValue().getName().equals(name))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        //@formatter:on
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return "TestCommandEnum{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
