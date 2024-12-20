/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.units.rpi.gps;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.gps.GPSEvent;
import com.robo4j.hw.rpi.gps.GPSListener;
import com.robo4j.hw.rpi.gps.PositionEvent;
import com.robo4j.hw.rpi.gps.VelocityEvent;
import com.robo4j.hw.rpi.serial.gps.MTK3339GPS;
import com.robo4j.math.geometry.Tuple3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Unit for getting GPS data.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class MtkGPSUnit extends RoboUnit<GPSRequest> {
    /**
     * This key configures the approximate update interval, or how often to
     * schedule reads from the serial port.
     */
    public static final String PROPERTY_KEY_READ_INTERVAL = "readInterval";

    /**
     * This key configures the scheduler to use for scheduling reads. Either
     * PLATFORM or INTERNAL. Use INTERNAL if the reads take too long and start
     * disrupting the platform scheduler too much.
     */
    public static final String PROPERTY_KEY_SCHEDULER = "scheduler";

    /**
     * Value for the scheduler key for using the platform scheduler.
     */
    public static final String PROPERTY_VALUE_PLATFORM_SCHEDULER = "platform";

    /**
     * Value for the scheduler key for using the internal scheduler.
     */
    public static final String PROPERTY_VALUE_INTERNAL_SCHEDULER = "internal";

    /**
     * This is the default value for the read interval.
     */
    public static final int DEFAULT_READ_INTERVAL = MTK3339GPS.DEFAULT_READ_INTERVAL;

    /**
     * This is the default value for the serial port.
     */
    public static final String DEFAULT_SERIAL_PORT = MTK3339GPS.DEFAULT_GPS_PORT;

    /**
     * This attribute will provide the state of the read interval.
     */
    public static final String ATTRIBUTE_NAME_READ_INTERVAL = "readInterval";

    public static final Collection<AttributeDescriptor<?>> KNOWN_ATTRIBUTES = Collections
            .unmodifiableCollection(List.of(DefaultAttributeDescriptor.create(Tuple3f.class, ATTRIBUTE_NAME_READ_INTERVAL)));
    private static final Logger LOGGER = LoggerFactory.getLogger(MtkGPSUnit.class);

    private final List<GPSEventListener> listeners = new ArrayList<>();
    private MTK3339GPS mtk3339gps;
    private String serialPort;
    private int readInterval = DEFAULT_READ_INTERVAL;

    // The future, if scheduled with the platform scheduler
    private volatile ScheduledFuture<?> scheduledFuture;

    private static class GPSEventListener implements GPSListener {
        private final RoboReference<GPSEvent> target;

        GPSEventListener(RoboReference<GPSEvent> target) {
            this.target = target;
        }

        @Override
        public void onPosition(PositionEvent event) {
            target.sendMessage(event);
        }

        @Override
        public void onVelocity(VelocityEvent event) {
            target.sendMessage(event);
        }
    }

    public MtkGPSUnit(RoboContext context, String id) {
        super(GPSRequest.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        serialPort = configuration.getString("serialPort", DEFAULT_SERIAL_PORT);
        readInterval = configuration.getInteger("readInterval", DEFAULT_READ_INTERVAL);
        String scheduler = configuration.getString("scheduler", PROPERTY_VALUE_PLATFORM_SCHEDULER);
        boolean usePlatformScheduler = PROPERTY_VALUE_PLATFORM_SCHEDULER.equals(scheduler);

        try {
            mtk3339gps = new MTK3339GPS(serialPort, readInterval);
        } catch (IOException e) {
            throw new ConfigurationException("Could not instantiate GPS!", e);
        }
        if (usePlatformScheduler) {
            scheduledFuture = getContext().getScheduler().scheduleAtFixedRate(() -> mtk3339gps.update(), 10, readInterval,
                    TimeUnit.MILLISECONDS);
        } else {
            mtk3339gps.start();
        }
    }

    @Override
    public <R> Future<R> getAttribute(AttributeDescriptor<R> attribute) {
        return super.getAttribute(attribute);
    }

    @Override
    public void onMessage(GPSRequest message) {
        super.onMessage(message);
        RoboReference<GPSEvent> targetReference = message.getTarget();
        switch (message.getOperation()) {
            case REGISTER -> register(targetReference);
            case UNREGISTER -> unregister(targetReference);
            default -> LOGGER.warn("Unknown operation:{}", message.getOperation());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
        if (descriptor.attributeType() == Integer.class && descriptor.attributeName().equals(ATTRIBUTE_NAME_READ_INTERVAL)) {
            return (R) Integer.valueOf(readInterval);
        }
        return super.onGetAttribute(descriptor);
    }

    @Override
    public void shutdown() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        mtk3339gps.shutdown();
        super.shutdown();
    }

    // Private Methods
    private synchronized void unregister(RoboReference<GPSEvent> targetReference) {
        List<GPSEventListener> copy = new ArrayList<>(listeners);
        for (GPSEventListener listener : copy) {
            if (targetReference.equals(listener.target)) {
                listeners.remove(listener);
                mtk3339gps.removeListener(listener);
                // I guess you could theoretically have several registered to
                // the same target, so let's keep checking...
            }
        }
    }

    private synchronized void register(RoboReference<GPSEvent> targetReference) {
        GPSEventListener listener = new GPSEventListener(targetReference);
        listeners.add(listener);
        mtk3339gps.addListener(listener);
    }
}
