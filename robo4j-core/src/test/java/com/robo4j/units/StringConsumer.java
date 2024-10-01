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

    public static final String DEFAULT_UNIT_NAME = "consumer";
    public static final String ATTR_TOTAL_SENT_MESSAGES = "getNumberOfSentMessages";
    public static final String ATTR_GET_RECEIVED_MESSAGES = "getReceivedMessages";
    public static final String ATTR_COUNT_DOWN_LATCH = "countDownLatch";
    public static final String PROP_TOTAL_MESSAGES = "totalNumberMessages";
    public static final DefaultAttributeDescriptor<CountDownLatch> DESCRIPTOR_COUNT_DOWN_LATCH = DefaultAttributeDescriptor
            .create(CountDownLatch.class, ATTR_COUNT_DOWN_LATCH);
    public static final DefaultAttributeDescriptor<Integer> DESCRIPTOR_TOTAL_MESSAGES = DefaultAttributeDescriptor
            .create(Integer.class, ATTR_TOTAL_SENT_MESSAGES);
    private static final int DEFAULT = 0;
    private final List<String> receivedMessages = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger counter;
    /* total messages latch */
    private CountDownLatch countDownLatch;

    /**
     * @param context context
     * @param id      id
     */
    public StringConsumer(RoboContext context, String id) {
        super(String.class, context, id);
        this.counter = new AtomicInteger(DEFAULT);
    }

    public List<String> getReceivedMessages() {
        return receivedMessages;
    }

    @Override
    public void onMessage(String message) {
        counter.incrementAndGet();
        receivedMessages.add(message);
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        int totalNumber = configuration.getInteger(PROP_TOTAL_MESSAGES, 0);
        if (totalNumber > 0) {
            countDownLatch = new CountDownLatch(totalNumber);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
        if (attribute.getAttributeName().equals(ATTR_TOTAL_SENT_MESSAGES)
                && attribute.getAttributeType() == Integer.class) {
            return (R) (Integer) counter.get();
        }
        if (attribute.getAttributeName().equals(ATTR_GET_RECEIVED_MESSAGES)
                && attribute.getAttributeType() == List.class) {
            return (R) receivedMessages;
        }
        if (attribute.getAttributeName().equals(ATTR_COUNT_DOWN_LATCH)
                && attribute.getAttributeType() == CountDownLatch.class) {
            return (R) countDownLatch;
        }
        return null;
    }

}
