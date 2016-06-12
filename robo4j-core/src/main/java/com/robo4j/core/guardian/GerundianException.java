package com.robo4j.core.guardian;

/**
 * Created by miroslavkopecky on 23/04/16.
 */
public class GerundianException extends RuntimeException {
    public GerundianException(String message) {
        super(message);
    }

    public GerundianException(String message, Throwable cause) {
        super(message, cause);
    }
}
