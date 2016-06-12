package com.robo4j.core.control;

/**
 * Created by miroslavkopecky on 16/04/16.
 */
public class ControlException extends RuntimeException {

    public ControlException(String message) {
        super(message);
    }

    public ControlException(String message, Throwable cause) {
        super(message, cause);
    }
}
