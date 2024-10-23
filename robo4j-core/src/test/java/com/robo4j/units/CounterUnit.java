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
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple unit which will count upwards from zero. Useful, for example, as a
 * heart beat generator.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CounterUnit extends RoboUnit<CounterCommand> {
    private final class CounterRunner implements Runnable {
        private final RoboReference<Integer> target;
        private final CountDownLatch latchReportMessages;

        private CounterRunner(RoboReference<Integer> target, CountDownLatch latchReportMessages) {
            this.target = target;
            this.latchReportMessages = latchReportMessages;
        }

        @Override
        public void run() {
            if (target != null) {
                LOGGER.debug("send message:{} to unit:{}", counter.get(), target);
                target.sendMessage(counter.getAndIncrement());
                latchReportMessages.countDown();
            } else {
                LOGGER.error("The target {} for the CounterUnit does not exist! Could not send count!", target);
            }
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CounterUnit.class);

    private final AtomicInteger counter = new AtomicInteger(0);
    private int interval;
    private int receivedMessagesOffset;


    public static final String ATTR_RECEIVED_MESSAGES_OFFSET = "receivedMessagesOffset";
    public static final String ATTR_COUNTER = "Counter";
    public static final String ATTR_REPORT_RECEIVED_MESSAGES_LATCH = "reportMessagesLatch";
    public static final String ATTR_PROCESS_ACTIVE = "processActive";

    public static final DefaultAttributeDescriptor<CountDownLatch> DESCRIPTOR_REPORT_RECEIVED_MESSAGES_LATCH = DefaultAttributeDescriptor
            .create(CountDownLatch.class, ATTR_REPORT_RECEIVED_MESSAGES_LATCH);
    public static final DefaultAttributeDescriptor<Integer> DESCRIPTOR_RECEIVED_MESSAGE_OFFSET = DefaultAttributeDescriptor
            .create(Integer.class, ATTR_RECEIVED_MESSAGES_OFFSET);
    public static final DefaultAttributeDescriptor<Boolean> DESCRIPTOR_PROCESS_DONE = DefaultAttributeDescriptor
            .create(Boolean.class, ATTR_PROCESS_ACTIVE);

    /**
     * This configuration key controls the interval between the updates, in ms.
     */
    public static final String KEY_INTERVAL = "interval";

    public static final String KEY_RECEIVED_MESSAGE = "reportMessages";

    /**
     * The default period, if no period is configured.
     */
    public static final int DEFAULT_INTERVAL = 1000;

    /**
     * The report latch when requested number of message is received
     */
    public static final int DEFAULT_RECEIVED_MESSAGE = 2;

    /**
     * This configuration key controls the target of the counter updates. This
     * configuration key is mandatory. Also, the target must exist when the
     * counter unit is started, and any change whilst running will be ignored.
     */
    public static final String KEY_TARGET = "target";

    /**
     * latch helps to identify the STOP state,
     */
    private CountDownLatch unitLatch = new CountDownLatch(1);

    /**
     * latch for reporting specific number of messages are received, by default it signal that any message was received
     */
    private CountDownLatch latchReportReceivedMessages = new CountDownLatch(1);

    /*
     * The currently running timer updater.
     */
    private ScheduledFuture<?> scheduledFuture;

    /*
     * The id of the target.
     */
    private String targetId;


    /**
     * Constructor.
     *
     * @param context the RoboContext.
     * @param id      the id of the RoboUnit.
     */
    public CounterUnit(RoboContext context, String id) {
        super(CounterCommand.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        interval = configuration.getInteger(KEY_INTERVAL, DEFAULT_INTERVAL);
        receivedMessagesOffset = configuration.getInteger(KEY_RECEIVED_MESSAGE, DEFAULT_RECEIVED_MESSAGE);
        latchReportReceivedMessages = new CountDownLatch(receivedMessagesOffset);
        targetId = configuration.getString(KEY_TARGET, null);
        if (targetId == null) {
            throw ConfigurationException.createMissingConfigNameException(KEY_TARGET);
        }
    }

    @Override
    public void onMessage(CounterCommand message) {
        synchronized (this) {
            super.onMessage(message);
            switch (message) {
                case START -> {
                    var counterUnitAction = new CounterRunner(getContext().getReference(targetId), latchReportReceivedMessages);
                    scheduledFuture = getContext().getScheduler().scheduleAtFixedRate(
                            counterUnitAction, 0, interval, TimeUnit.MILLISECONDS);
                }

                case STOP -> {
                    if (scheduledFuture.cancel(false)) {
                        unitLatch.countDown();
                    } else {
                        scheduledFuture.cancel(true);
                        LOGGER.error("scheduled feature could not be properly cancelled!");
                    }
                }
                case RESET -> {
                    counter.set(0);
                }
                case COUNTER_INC -> receivedMessagesOffset++;

            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
        if (attribute.getAttributeName().equals(ATTR_RECEIVED_MESSAGES_OFFSET) && attribute.getAttributeType() == Integer.class) {
            return (R) (Integer) receivedMessagesOffset;
        }
        if (attribute.getAttributeName().equals(ATTR_COUNTER) && attribute.getAttributeType() == Integer.class) {
            return (R) (Integer) counter.get();
        }
        if (attribute.getAttributeName().equals(ATTR_REPORT_RECEIVED_MESSAGES_LATCH)
                && attribute.getAttributeType() == CountDownLatch.class) {
            return (R) latchReportReceivedMessages;
        }
        if (attribute.getAttributeName().equals(ATTR_PROCESS_ACTIVE)
                && attribute.getAttributeType() == Boolean.class) {
            return (R) (Boolean) scheduledFuture.isDone();
        }
        return null;
    }

}
