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
package com.robo4j.units;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ConfigurationConsumer extends RoboUnit<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationConsumer.class);
    private static final int DEFAULT = 0;
    private final AtomicInteger counter;
    private final List<String> receivedMessages = new ArrayList<>();

    /**
     * @param context RoboContext
     * @param id      unit id
     */
    public ConfigurationConsumer(RoboContext context, String id) {
        super(String.class, context, id);
        this.counter = new AtomicInteger(DEFAULT);
    }

    public synchronized List<String> getReceivedMessages() {
        return receivedMessages;
    }

    @Override
    public synchronized void onMessage(String message) {
        counter.incrementAndGet();
        receivedMessages.add(message);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        LOGGER.info("configuration:{}", configuration);
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
        if (attribute.attributeName().equals("getNumberOfSentMessages") && attribute.attributeType() == Integer.class) {
            return (R) (Integer) counter.get();
        }
        if (attribute.attributeName().equals("getReceivedMessages")
                && attribute.attributeType() == ArrayList.class) {
            return (R) receivedMessages;
        }
        return null;
    }

}
