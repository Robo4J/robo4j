package com.robo4j.socket.http.dto;

import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ClassFieldValueDTO {
    private String name;
    private FieldValueDTO value;


    public ClassFieldValueDTO(String name, FieldValueDTO value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public FieldValueDTO getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassFieldValueDTO that = (ClassFieldValueDTO) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return "ClassFieldValueDTO{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
