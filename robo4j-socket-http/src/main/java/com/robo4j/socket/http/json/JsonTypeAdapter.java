package com.robo4j.socket.http.json;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface JsonTypeAdapter<T> {

    String adapt(T obj);

}
