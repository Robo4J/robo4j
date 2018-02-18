package com.robo4j.socket.http.message;

/**
 * Datagram message
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface DatagramMessage<T> {
    T toMessage();

    void addMessage(T message);
}
