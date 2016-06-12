package com.robo4j.core.sensor.state;

import com.robo4j.commons.concurrent.TransferSignal;
import com.robo4j.core.sensor.SensorType;

/**
 * Created by miroslavkopecky on 22/05/15.
 */
public interface SensorState extends TransferSignal, Comparable<SensorDefaultState> {

    Long getTimestamp();
    SensorType getType();
    String getNumberValue();
    /**
     * used for testing purposes
     * @return - String
     */
    String getTestValue();

    int getPriority();
}
