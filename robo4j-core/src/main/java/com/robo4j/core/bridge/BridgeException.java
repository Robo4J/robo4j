package com.robo4j.core.bridge;

/**
 * Created by miroslavkopecky on 30/04/16.
 */
public class BridgeException extends RuntimeException {

    public BridgeException(String message) {
        super(message);
    }

    public BridgeException(String message, Throwable cause) {
        super(message, cause);
    }
}
