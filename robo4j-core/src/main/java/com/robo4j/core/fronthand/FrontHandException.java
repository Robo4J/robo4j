package com.robo4j.core.fronthand;

/**
 * Created by miroslavkopecky on 27/04/16.
 */
public class FrontHandException extends RuntimeException {

    public FrontHandException(String message) {
        super(message);
    }

    public FrontHandException(String message, Throwable cause) {
        super(message, cause);
    }
}
