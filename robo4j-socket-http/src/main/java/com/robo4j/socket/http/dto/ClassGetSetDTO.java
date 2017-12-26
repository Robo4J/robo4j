package com.robo4j.socket.http.dto;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ClassGetSetDTO {
    private final String name;
    private final Field field;
    private final boolean map;
    private final Method getMethod;
    private final Method setMethod;

    public ClassGetSetDTO(String name, Field field, Method getMethod, Method setMethod) {
        this(name, field, false, getMethod, setMethod);
    }

    public ClassGetSetDTO(String name, Field field, boolean map, Method getMethod, Method setMethod) {
        this.name = name;
        this.field = field;
        this.map = map;
        this.getMethod = getMethod;
        this.setMethod = setMethod;
    }

    public Field getField() {
        return field;
    }

    public boolean isMap() {
        return map;
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
        return map == that.map &&
                Objects.equals(name, that.name) &&
                Objects.equals(field, that.field) &&
                Objects.equals(getMethod, that.getMethod) &&
                Objects.equals(setMethod, that.setMethod);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, field, map, getMethod, setMethod);
    }

    @Override
    public String toString() {
        return "ClassGetSetDTO{" +
                "name='" + name + '\'' +
                ", field=" + field +
                ", map=" + map +
                ", getMethod=" + getMethod +
                ", setMethod=" + setMethod +
                '}';
    }
}
