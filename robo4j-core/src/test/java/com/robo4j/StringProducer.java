/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
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
package com.robo4j;

import com.robo4j.configuration.Configuration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class StringProducer extends RoboUnit<String> {

    public static final String ATTR_GET_NUMBER_OF_SENT_MESSAGES = "getNumberOfSentMessages";
    public static final String ATTR_COUNT_DOWN_LATCH = "countDownLatch";
    public static final DefaultAttributeDescriptor<Integer> DESCRIPTOR_TOTAL_MESSAGES = DefaultAttributeDescriptor
            .create(Integer.class, ATTR_GET_NUMBER_OF_SENT_MESSAGES);
    public static final DefaultAttributeDescriptor<CountDownLatch> DESCRIPTOR_COUNT_DOWN_LATCH = DefaultAttributeDescriptor
            .create(CountDownLatch.class, ATTR_COUNT_DOWN_LATCH);
    /* default sent messages */
    private static final int DEFAULT = 0;
    public static final String PROPERTY_SEND_RANDOM_MESSAGE = "sendRandomMessage";
    public static final String PROP_TOTAL_MESSAGES = "totalMessages";
    public static final String PROP_TARGET = "target";
    private CountDownLatch latch;
    private AtomicInteger counter;
    private String target;


    /**
     * @param context
     * @param id
     */
    public StringProducer(RoboContext context, String id) {
        super(String.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString(PROP_TARGET, null);
        if (target == null) {
            throw ConfigurationException.createMissingConfigNameException("target");
        }
        Integer totalMessages = configuration.getInteger(PROP_TOTAL_MESSAGES, null);
        if(totalMessages == null){
            throw ConfigurationException.createMissingConfigNameException(PROP_TOTAL_MESSAGES);
        }
        counter = new AtomicInteger(DEFAULT);
        latch = new CountDownLatch(totalMessages);

    }

    @Override
    public void onMessage(String message) {
        if (message == null) {
            System.out.println("No Message!");
        } else {
            counter.incrementAndGet();
            String[] input = message.split("::");
            String messageType = input[0];
            switch (messageType) {
                case PROPERTY_SEND_RANDOM_MESSAGE:
                    sendRandomMessage();
                    latch.countDown();
                    break;
                default:
                    System.out.println("don't understand message: " + message);

            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
        if (attribute.getAttributeName().equals(ATTR_GET_NUMBER_OF_SENT_MESSAGES) && attribute.getAttributeType() == Integer.class) {
            return (R) (Integer) counter.get();
        }
        if(attribute.getAttributeName().equals(ATTR_COUNT_DOWN_LATCH) && attribute.getAttributeType() == CountDownLatch.class){
            return (R) latch;
        }
        return null;
    }

    public void sendRandomMessage() {
        final String message = StringToolkit.getRandomMessage(10);
        getContext().getReference(target).sendMessage(message);
    }

}
