package com.robo4j.core.platform;

/**
 * Created by miroslavkopecky on 23/04/16.
 */
public class PlatformException extends RuntimeException {

    public PlatformException(String message) {
        super(message);
    }

    public PlatformException(String message, Throwable cause) {
        super(message, cause);
    }
}
