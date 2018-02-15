package com.robo4j.socket.http.util;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum DatagramType {
    JSON    (1)
    ;

    private final int type;

    DatagramType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "DatagramType{" +
                "type=" + type +
                '}';
    }
}
