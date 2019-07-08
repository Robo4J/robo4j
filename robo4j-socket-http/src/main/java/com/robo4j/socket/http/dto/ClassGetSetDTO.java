/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.dto;

import com.robo4j.socket.http.util.TypeCollection;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ClassGetSetDTO {
    private final String name;
    private final Class<?> valueClass;
    private final TypeCollection collection;
    private final Method getMethod;
    private final Method setMethod;

    public ClassGetSetDTO(String name, Class<?> valueClass, Method getMethod, Method setMethod) {
        this(name, valueClass, null, getMethod, setMethod);
    }

    public ClassGetSetDTO(String name, Class<?> valueClass, TypeCollection collection, Method getMethod, Method setMethod) {
        this.name = name;
        this.valueClass = valueClass;
        this.collection = collection;
        this.getMethod = getMethod;
        this.setMethod = setMethod;
    }

    public Class<?> getValueClass() {
        return valueClass;
    }

    public TypeCollection getCollection() {
        return collection;
    }

    public String getName() {
        return name;
    }

    public Method getGetMethod() {
        return getMethod;
    }

    public Method getSetMethod() {
        return setMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassGetSetDTO that = (ClassGetSetDTO) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(valueClass, that.valueClass) &&
                collection == that.collection &&
                Objects.equals(getMethod, that.getMethod) &&
                Objects.equals(setMethod, that.setMethod);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, valueClass, collection, getMethod, setMethod);
    }

    @Override
    public String toString() {
        return "ClassGetSetDTO{" +
                "name='" + name + '\'' +
                ", valueClass=" + valueClass +
                ", collection=" + collection +
                ", getMethod=" + getMethod +
                ", setMethod=" + setMethod +
                '}';
    }
}
