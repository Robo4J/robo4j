package com.robo4j.core.bridge.command.cache;

/**
 * Created by miroslavkopecky on 18/04/16.
 */
public class CacheException extends RuntimeException {

    public CacheException(String message) {
        super(message);
    }

    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
