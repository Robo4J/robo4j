package com.robo4j.socket.http.units.test.codec;

import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class TestPerson {
    private String name;
    private Integer value;
    private TestPerson child;

    public TestPerson() {
    }

    public TestPerson(String name, Integer value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public TestPerson getChild() {
        return child;
    }

    public void setChild(TestPerson child) {
        this.child = child;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestPerson that = (TestPerson) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(value, that.value) &&
                Objects.equals(child, that.child);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, value, child);
    }

    @Override
    public String toString() {
        return "TestPerson{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", child=" + child +
                '}';
    }
}
