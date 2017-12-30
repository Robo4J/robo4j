package com.robo4j.socket.http.util;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboReflectException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RoboReflectException(String message) {
        super(message);
    }

    public RoboReflectException(String message, Throwable cause) {
        super(message, cause);
    }
}
