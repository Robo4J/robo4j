/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This RegistryTypeEnum.java  is part of robo4j.
 *
 *  robo4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  robo4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.commons.enums;

/**
 * @author Miro Kopecky (@miragemiko)
 * @since 29.11.2016
 */
public enum RegistryTypeEnum {

    //@formatter:on
    ENGINES             (1, "engines"),
    SENSORS             (2, "sensors"),
    UNITS               (3, "units"),
    SERVICES            (4, "services"),
    ;
    //@formatter:off


    int id;
    String name;

    RegistryTypeEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "RegistryTypeEnum{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
