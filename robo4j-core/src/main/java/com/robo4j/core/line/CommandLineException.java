package com.robo4j.core.line;

/**
 * Created by miroslavkopecky on 23/04/16.
 */
public class CommandLineException extends RuntimeException {
    public CommandLineException(String message) {
        super(message);
    }

    public CommandLineException(String message, Throwable cause) {
        super(message, cause);
    }
}
