package com.robo4j.core.bridge.command;

/**
 * Created by miroslavkopecky on 24/04/16.
 */
public class CommandException extends RuntimeException {

    public CommandException(String message) {
        super(message);
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
