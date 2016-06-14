/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This SensorStateCheckBusQueue.java is part of robo4j.
 *
 *     robo4j is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     robo4j is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
