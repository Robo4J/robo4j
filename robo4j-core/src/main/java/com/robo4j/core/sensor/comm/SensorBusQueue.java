package com.robo4j.core.sensor.comm;

import com.robo4j.commons.concurrent.CoreBusQueue;

/**
 * Created by miroslavkopecky on 14/04/16.
 */
public class SensorBusQueue<TransferType> extends CoreBusQueue {

    public SensorBusQueue(int awaitSeconds){
        super(awaitSeconds);
    }
}
