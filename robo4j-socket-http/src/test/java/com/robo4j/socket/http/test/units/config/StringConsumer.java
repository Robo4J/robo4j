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
package com.robo4j.socket.http.test.units.config;

import com.robo4j.*;
import com.robo4j.configuration.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

public class StringConsumer extends RoboUnit<String> {
    public static final String NAME = "stringConsumer";
    public static final String ATTR_MESSAGES_TOTAL = "getNumberOfSentMessages";
    public static final String ATTR_RECEIVED_MESSAGES = "getReceivedMessages";
    public static final String ATTR_MESSAGES_LATCH = "messagesLatch";
    public static final String PROP_TOTAL_NUMBER_MESSAGES = "totalNumberMessages";

    public static final DefaultAttributeDescriptor<CountDownLatch> DESCRIPTOR_MESSAGES_LATCH = DefaultAttributeDescriptor
            .create(CountDownLatch.class, ATTR_MESSAGES_LATCH);
    public static final DefaultAttributeDescriptor<Integer> DESCRIPTOR_MESSAGES_TOTAL = DefaultAttributeDescriptor
            .create(Integer.class, ATTR_MESSAGES_TOTAL);

    private static final int DEFAULT = 0;
    private final AtomicInteger counter = new AtomicInteger(DEFAULT);
    private final List<String> receivedMessages = Collections.synchronizedList(new ArrayList<>());
    private CountDownLatch messagesLatch;

    /**
     * @param context robo4j context
     * @param id unit id
     */
    public StringConsumer(RoboContext context, String id) {
        super(String.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        int totalNumber = configuration.getInteger(PROP_TOTAL_NUMBER_MESSAGES, 0);
        if (totalNumber > 0) {
            messagesLatch = new CountDownLatch(totalNumber);
        }
    }

    @Override
    public void onMessage(String message) {
        counter.incrementAndGet();
        receivedMessages.add(message);
        if (messagesLatch != null) {
            messagesLatch.countDown();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
        if (attribute.attributeName().equals(ATTR_MESSAGES_TOTAL)
                && attribute.attributeType() == Integer.class) {
            return (R) Integer.valueOf(counter.get());
        }
        if (attribute.attributeName().equals(ATTR_RECEIVED_MESSAGES)
                && attribute.attributeType() == List.class) {
            return (R) receivedMessages;
        }
        if (attribute.attributeName().equals(ATTR_MESSAGES_LATCH)
                && attribute.attributeType() == CountDownLatch.class) {
            return (R) messagesLatch;
        }
        return null;
    }

}
