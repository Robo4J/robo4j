package com.robo4j.core.sensor.comm;

import com.robo4j.core.lego.LegoBrickRemoteProvider;
import com.robo4j.commons.concurrent.QueueFIFOEntry;
import com.robo4j.core.lego.LegoBrickPropertiesHolder;
import com.robo4j.core.sensor.SensorType;
import com.robo4j.core.sensor.provider.SensorProvider;
import com.robo4j.core.sensor.provider.SensorProviderImpl;
import com.robo4j.core.sensor.state.SensorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by miroslavkopecky on 21/02/16.
 */
public class SensorStateCheckBusQueue implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SensorStateCheckBusQueue.class);

    private LegoBrickRemoteProvider legoBrickRemoteProvider;
    private LegoBrickPropertiesHolder legoBrickPropertiesHolder;
    private final SensorType type;
    private final SensorBusQueue<QueueFIFOEntry<? extends SensorState>> bus;
    private final SensorProvider sensorProvider;

    public SensorStateCheckBusQueue(LegoBrickRemoteProvider legoBrickRemoteProvider,
                                    LegoBrickPropertiesHolder legoBrickPropertiesHolder, SensorType type,
                                    SensorBusQueue<QueueFIFOEntry<? extends SensorState>> bus) {
        this.legoBrickRemoteProvider = legoBrickRemoteProvider;
        this.legoBrickPropertiesHolder = legoBrickPropertiesHolder;
        this.type = type;
        this.bus = bus;
        this.sensorProvider = new SensorProviderImpl(legoBrickRemoteProvider);
    }

    @SuppressWarnings(value = "unchecked")
    @Override
    public void run() {
        try {
            final SensorState state = sensorProvider.connect(type);
            logger.info("RUN state= " + state);
            bus.transfer(new QueueFIFOEntry(state));
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

}
