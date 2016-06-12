package com.robo4j.core.sensor.state;

import com.robo4j.core.sensor.SensorType;

/**
 * Created by miroslavkopecky on 22/05/15.
 */
public class SensorDefaultState implements SensorState  {

    private final SensorType type;
    private final Long timestamp;
    private final String value;
    private final int priority;


    public SensorDefaultState(SensorType type, Long timestamp, String value, Integer priority) {
        this.type = type;
        this.timestamp = timestamp;
        this.value = value;
        this.priority = priority;
    }

    public SensorType getType() {
        return type;
    }

    @Override
    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getNumberValue() {
        return value;
    }

    @Override
    public String getTestValue() {
        return value;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(SensorDefaultState o) {
        return (this.priority > o.getPriority()) ? 1 : (this.priority < o.getPriority()) ? -1 : 0;
    }

    @Override
    public String toString() {
        return "SensorDefaultState{" +
                "type=" + type +
                ", timestamp=" + timestamp +
                ", value='" + value + '\'' +
                '}';
    }
}
