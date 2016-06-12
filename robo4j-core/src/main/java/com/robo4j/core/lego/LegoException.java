package com.robo4j.core.lego;

/**
 * Created by miroslavkopecky on 11/04/16.
 */
public class LegoException extends RuntimeException {

    public LegoException() {
    }

    public LegoException(String message) {
        super(message);
    }

    public LegoException(String message, Throwable cause) {
        super(message, cause);
    }

    public LegoException(Throwable cause) {
        super(cause);
    }
}
