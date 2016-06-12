package com.robo4j.core.sensor.provider;

import com.robo4j.core.sensor.SensorType;
import com.robo4j.core.sensor.state.SensorState;

/**
 * Created by miroslavkopecky on 24/02/16.
 */
public interface SensorProvider {

    SensorState connect(SensorType type);
}
