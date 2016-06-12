package com.robo4j.core.properties;

/**
 * Created by miroslavkopecky on 23/04/16.
 */
public class PropertiesException extends RuntimeException {

    public PropertiesException(String message) {
        super(message);
    }

    public PropertiesException(String message, Throwable cause) {
        super(message, cause);
    }
}
