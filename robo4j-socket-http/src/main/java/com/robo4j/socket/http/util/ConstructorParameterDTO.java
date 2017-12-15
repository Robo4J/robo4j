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
