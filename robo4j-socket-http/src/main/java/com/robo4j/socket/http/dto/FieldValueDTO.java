package com.robo4j.socket.http.dto;

import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class FieldValueDTO {
    private Class<?> clazz;
    private Object value;

    public FieldValueDTO(Class<?> clazz, Object value) {
        this.clazz = clazz;
        this.value = value;
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
        FieldValueDTO that = (FieldValueDTO) o;
        return Objects.equals(clazz, that.clazz) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(clazz, value);
    }

    @Override
    public String toString() {
        return "FieldValueDTO{" +
                "clazz=" + clazz +
                ", value=" + value +
                '}';
    }
}
