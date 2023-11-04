/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.test.units.config;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class PropertyListBuilder<Type> {

    private List<Type> list;

    private PropertyListBuilder() {
        this.list = new LinkedList<>();
    }

    @SuppressWarnings("rawtypes")
    public static <Type> PropertyListBuilder Builder() {
        return new PropertyListBuilder<Type>();
    }

    @SuppressWarnings("rawtypes")
    public PropertyListBuilder add(Type element) {
        list.add(element);
        return this;
    }

    public List<Type> build() {
        return Collections.unmodifiableList(list);
    }

}
