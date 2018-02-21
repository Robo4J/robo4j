package com.robo4j.socket.http.util;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum DatagramBodyType {
    JSON    (1),
    BYTE    (2),
    TEXT    (3)
    ;

    private final int type;

    DatagramBodyType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "DatagramBodyType{" +
                "type=" + type +
                '}';
    }
}
