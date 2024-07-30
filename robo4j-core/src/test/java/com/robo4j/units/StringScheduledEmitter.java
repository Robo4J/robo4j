/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
import com.robo4j.util.StringConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class StringScheduledEmitter extends RoboUnit<String> {

    public static final String DEFAULT_UNIT_NAME = "scheduledEmitter";
    public static final String ATTR_GET_NUMBER_OF_SENT_MESSAGES = "getNumberOfSentMessages";
    public static final String ATTR_COUNT_DOWN_LATCH = "countDownLatch";
    public static final DefaultAttributeDescriptor<Integer> DESCRIPTOR_TOTAL_MESSAGES = DefaultAttributeDescriptor
            .create(Integer.class, ATTR_GET_NUMBER_OF_SENT_MESSAGES);
    public static final DefaultAttributeDescriptor<CountDownLatch> DESCRIPTOR_COUNT_DOWN_LATCH = DefaultAttributeDescriptor
            .create(CountDownLatch.class, ATTR_COUNT_DOWN_LATCH);
    /* default sent messages */
    private static final int DEFAULT = 0;
    private static final long DEFAULT_INIT_DELAY_MILLS = 10;
    public static final String PROP_TARGET = "target";
    public static final String PROP_INIT_DELAY = "initSchedulerDelay";
    public static final String PROP_PERIOD = "schedulerPeriod";
    private CountDownLatch latch;
    private AtomicInteger counter;
    private String target;
    private Long initSchedulerDelayMillis;
    private Long schedulerPeriodMillis;
    private final Map<String, String> messages = new ConcurrentHashMap<>(10);


    /**
     * @param context context
     * @param id      identifier
     */
    public StringScheduledEmitter(RoboContext context, String id) {
        super(String.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString(PROP_TARGET, null);
        if (target == null) {
            throw ConfigurationException.createMissingConfigNameException(PROP_TARGET);
        }
        initSchedulerDelayMillis = configuration.getLong(PROP_INIT_DELAY, DEFAULT_INIT_DELAY_MILLS);
        schedulerPeriodMillis = configuration.getLong(PROP_PERIOD, DEFAULT_INIT_DELAY_MILLS);

        messages.put(target, StringConstants.EMPTY);
    }

    @Override
    public void start() {
        System.out.println("start scheduler");
        getContext().getScheduler().scheduleAtFixedRate(() -> {
            var messageForTarget = messages.get(target);
            System.out.println("scheduler message: " + messageForTarget + ", target: " + target);
            getContext().getReference(target).sendMessage(messageForTarget);
        }, initSchedulerDelayMillis, schedulerPeriodMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onMessage(String message) {
        messages.replace(target, message);
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
        if (attribute.getAttributeName().equals(ATTR_GET_NUMBER_OF_SENT_MESSAGES) && attribute.getAttributeType() == Integer.class) {
            return (R) (Integer) counter.get();
        }
        if (attribute.getAttributeName().equals(ATTR_COUNT_DOWN_LATCH) && attribute.getAttributeType() == CountDownLatch.class) {
            return (R) latch;
        }
        return null;
    }
}
