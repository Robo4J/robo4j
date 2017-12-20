package com.robo4j.socket.http.units.test.codec;

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
    public String toString() {
        return "TestPerson{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", child=" + child +
                '}';
    }
}
