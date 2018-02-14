package com.robo4j.socket.http.message;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface MessageDenominator<T> {
    T generate();
}
