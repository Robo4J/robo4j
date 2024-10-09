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
package com.robo4j.net;

import com.robo4j.*;
import com.robo4j.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RemoteTestMessageProducer extends RoboUnit<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteTestMessageProducer.class);
    public static final String ATTR_TOTAL_MESSAGES = "getNumberOfSentMessages";
    public static final String ATTR_COUNT_DOWN_LATCH = "countDownLatch";
    public static final String ATTR_ACK_LATCH = "ackLatch";
    public static final String PROP_TOTAL_NUMBER_MESSAGES = "totalNumberMessages";
    public static final String ATTR_ACK = "acknowledge";
    public static final String ATTR_SEND_MESSAGE = "sendMessage";
    public static final DefaultAttributeDescriptor<CountDownLatch> DESCRIPTOR_COUNT_DOWN_LATCH = DefaultAttributeDescriptor
            .create(CountDownLatch.class, ATTR_COUNT_DOWN_LATCH);
    public static final DefaultAttributeDescriptor<CountDownLatch> DESCRIPTOR_ACK_LATCH = DefaultAttributeDescriptor
            .create(CountDownLatch.class, ATTR_ACK_LATCH);
    public static final DefaultAttributeDescriptor<Integer> DESCRIPTOR_TOTAL_ACK = DefaultAttributeDescriptor
            .create(Integer.class, ATTR_ACK);
    /* default sent messages */
    private static final int DEFAULT = 0;
    public static final String PROP_TARGET = "target";
    public static final String PROP_TARGET_CONTEXT = "targetContext";
    private volatile AtomicInteger ackCounter;
    private volatile AtomicInteger totalCounter;
    private CountDownLatch countDownLatch;
    private CountDownLatch ackLatch;
    private String target;
    private String targetContext;


    /**
     * @param context RoboContext
     * @param id      unit id
     */
    public RemoteTestMessageProducer(RoboContext context, String id) {
        super(String.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString(PROP_TARGET, null);
        if (target == null) {
            throw ConfigurationException.createMissingConfigNameException(PROP_TARGET);
        }
        targetContext = configuration.getString(PROP_TARGET_CONTEXT, null);
        if (targetContext == null) {
            throw ConfigurationException.createMissingConfigNameException(PROP_TARGET_CONTEXT);
        }
        totalCounter = new AtomicInteger(DEFAULT);
        ackCounter = new AtomicInteger(DEFAULT);
        int totalNumber = configuration.getInteger(PROP_TOTAL_NUMBER_MESSAGES, 0);
        if (totalNumber > 0) {
            countDownLatch = new CountDownLatch(totalNumber);
            ackLatch = new CountDownLatch(totalNumber);
        }
    }

    @Override
    public void onMessage(String message) {
        if (message == null) {
            LOGGER.info("No Message!");
        } else {
            String[] input = message.split("::");
            String messageType = input[0];
            switch (messageType) {
                case ATTR_SEND_MESSAGE:
                    totalCounter.incrementAndGet();
                    if (countDownLatch != null) {
                        countDownLatch.countDown();
                    }
                    sendRandomMessage();
                    break;
                case ATTR_ACK:
                    ackCounter.incrementAndGet();
                    if (ackLatch != null) {
                        ackLatch.countDown();
                    }
                    break;
                default:
                    LOGGER.error("don't understand message: {}", message);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
        if (attribute.getAttributeName().equals(ATTR_TOTAL_MESSAGES) && attribute.getAttributeType() == Integer.class) {
            return (R) (Integer) totalCounter.get();
        }
        if (attribute.getAttributeName().equals(ATTR_COUNT_DOWN_LATCH)
                && attribute.getAttributeType() == CountDownLatch.class) {
            return (R) countDownLatch;
        }
        if (attribute.getAttributeName().equals(ATTR_ACK_LATCH)
                && attribute.getAttributeType() == CountDownLatch.class) {
            return (R) ackLatch;
        }
        if (attribute.getAttributeName().equals(ATTR_ACK) && attribute.getAttributeType() == Integer.class) {
            return (R) Integer.valueOf(ackCounter.get());
        }
        return null;
    }

    public void sendRandomMessage() {
        final int number = TestToolkit.getRND().nextInt() % 100;
        final String text = StringToolkit.getRandomMessage(10);

        // We're sending a reference to ourself for getting the acks...
        final TestMessageType message = new TestMessageType(number, text, getContext().getReference(getId()));
        RoboContext ctx = LookupServiceProvider.getDefaultLookupService().getContext(targetContext);
        ctx.getReference(target).sendMessage(message);
    }

    public int getAckCount() {
        return ackCounter.get();
    }
}
