package com.robo4j.core.sensor.state;

/**
 * Created by miroslavkopecky on 23/04/16.
 */
public class SensorException extends RuntimeException {

    public SensorException(String message) {
        super(message);
    }

    public SensorException(String message, Throwable cause) {
        super(message, cause);
    }
}
