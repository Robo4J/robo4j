package com.robo4j.socket.http.units;

/**
 * interface for http context
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface HttpContext<T> {

    boolean isEmpty();

    boolean containsPath(String path);

    T getPathConfig(String path);



}
