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
package com.robo4j.socket.http.util;

import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ConstructorParameterDTO {
    private String name;
    private Class<?> clazz;
    private Object value;

    public ConstructorParameterDTO(String name, Class<?> clazz, Object value) {
        this.name = name;
        this.clazz = clazz;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstructorParameterDTO that = (ConstructorParameterDTO) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(clazz, that.clazz) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, clazz, value);
    }

    @Override
    public String toString() {
        return "ConstructorParameterDTO{" +
                "name='" + name + '\'' +
                ", clazz=" + clazz +
                ", value=" + value +
                '}';
    }
}
