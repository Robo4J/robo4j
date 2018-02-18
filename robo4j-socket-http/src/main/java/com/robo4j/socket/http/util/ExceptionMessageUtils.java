package com.robo4j.socket.http.util;

/**
 * formatting message for exception
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ExceptionMessageUtils {

    public static String mapMessage(Object key, Object val){
        return String.format("invalid key: %s or value %s", key, val);
    }
}
