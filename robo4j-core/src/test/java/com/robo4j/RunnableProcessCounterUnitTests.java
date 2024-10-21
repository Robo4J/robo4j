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
package com.robo4j;

import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.units.CounterCommand;
import com.robo4j.units.CounterUnit;
import com.robo4j.units.IntegerConsumer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for the CounterUnit.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class RunnableProcessCounterUnitTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(RunnableProcessCounterUnitTests.class);
    private static final int TIMEOUT_MIN = 2;
    private static final String COUNTER_PRODUCER_ID = "counter";
    private static final String CONSUMER_ID = "consumer";
    private static final AttributeDescriptor<Integer> NUMBER_OF_MESSAGES = new DefaultAttributeDescriptor<>(Integer.class,
            "NumberOfReceivedMessages");
    private static final AttributeDescriptor<Integer> COUNTER = new DefaultAttributeDescriptor<>(Integer.class, CounterUnit.ATTR_COUNTER);

    @SuppressWarnings({"unchecked"})
    private static final AttributeDescriptor<ArrayList<Integer>> MESSAGES = new DefaultAttributeDescriptor<>(
            (Class<ArrayList<Integer>>) new ArrayList<Integer>().getClass(),
            "ReceivedMessages");

    @Test
    void runnableProcessStartTest() throws RoboBuilderException {
        RoboBuilder builder = new RoboBuilder();
        builder.add(IntegerConsumer.class, CONSUMER_ID);
        builder.add(CounterUnit.class, getCounterConfiguration(CONSUMER_ID, 1000), COUNTER_PRODUCER_ID);
        RoboContext context = builder.build();

        context.start();

        assertEquals(LifecycleState.STARTED, context.getState());
    }

    @Test
    void runnableGetProcessReferenceTest() throws RoboBuilderException, ExecutionException, InterruptedException, TimeoutException {
        var counterMessageProducerInterval = 1000;
        RoboBuilder builder = new RoboBuilder();
        builder.add(IntegerConsumer.class, CONSUMER_ID);
        builder.add(CounterUnit.class, getCounterConfiguration(CONSUMER_ID, counterMessageProducerInterval), COUNTER_PRODUCER_ID);
        RoboContext context = builder.build();
        context.start();

        RoboReference<CounterCommand> messageProducer = context.getReference(COUNTER_PRODUCER_ID);
        messageProducer.sendMessage(CounterCommand.START);
        var latchCreatedMessagesInInterval = getAttributeOrTimeout(messageProducer, CounterUnit.DESCRIPTOR_REPORT_RECEIVED_MESSAGES_LATCH);
        var createdMessages = latchCreatedMessagesInInterval.await(TIMEOUT_MIN, TimeUnit.MINUTES);

        RoboReference<Integer> consumer = context.getReference(CONSUMER_ID);

        var receivedMessages = getAttributeOrTimeout(consumer, NUMBER_OF_MESSAGES);

        assertTrue(createdMessages);
        assertEquals(CounterUnit.DEFAULT_RECEIVED_MESSAGE, receivedMessages);
    }

    @Test
    void runnableGetStoppedProcessReferenceTest() throws RoboBuilderException, ExecutionException, InterruptedException, TimeoutException {
        var counterMessageProducerInterval = 1000;
        RoboBuilder builder = new RoboBuilder();
        builder.add(IntegerConsumer.class, CONSUMER_ID);
        builder.add(CounterUnit.class, getCounterConfiguration(CONSUMER_ID, counterMessageProducerInterval), COUNTER_PRODUCER_ID);
        RoboContext context = builder.build();
        context.start();

        RoboReference<CounterCommand> messageProducer = context.getReference(COUNTER_PRODUCER_ID);
        messageProducer.sendMessage(CounterCommand.START);
        var latchCreatedMessagesInInterval = getAttributeOrTimeout(messageProducer, CounterUnit.DESCRIPTOR_REPORT_RECEIVED_MESSAGES_LATCH);
        var createdMessages = latchCreatedMessagesInInterval.await(TIMEOUT_MIN, TimeUnit.MINUTES);
        messageProducer.sendMessage(CounterCommand.STOP);

        RoboReference<Integer> consumer = context.getReference(CONSUMER_ID);
        var receivedMessages = getAttributeOrTimeout(consumer, NUMBER_OF_MESSAGES);


        assertTrue(createdMessages);
        assertEquals(CounterUnit.DEFAULT_RECEIVED_MESSAGE, receivedMessages);
        assertEquals(LifecycleState.STARTED, context.getState());
    }

    @Test
    void test() throws RoboBuilderException, InterruptedException, ExecutionException {
        // FIXME(Marcus/Aug 20, 2017): We really should get rid of the sleeps
        // here and use waits with timeouts...
        RoboBuilder builder = new RoboBuilder();
        builder.add(IntegerConsumer.class, CONSUMER_ID);
        builder.add(CounterUnit.class, getCounterConfiguration(CONSUMER_ID, 1000), COUNTER_PRODUCER_ID);
        RoboContext context = builder.build();
        context.start();
        assertEquals(LifecycleState.STARTED, context.getState());
        RoboReference<CounterCommand> counter = context.getReference(COUNTER_PRODUCER_ID);
        RoboReference<Integer> consumer = context.getReference(CONSUMER_ID);
        counter.sendMessage(CounterCommand.START);
        Thread.sleep(2500);
        assertTrue(consumer.getAttribute(NUMBER_OF_MESSAGES).get() > 2);
        counter.sendMessage(CounterCommand.STOP);
        Thread.sleep(200);
        Integer count = consumer.getAttribute(NUMBER_OF_MESSAGES).get();
        Thread.sleep(2500);
        assertEquals(count, consumer.getAttribute(NUMBER_OF_MESSAGES).get());
        ArrayList<Integer> messages = consumer.getAttribute(MESSAGES).get();
        assertNotEquals(0, messages.size());
        assertNotEquals(0, (int) messages.get(messages.size() - 1));
        counter.sendMessage(CounterCommand.RESET);
        Thread.sleep(1000);
        assertEquals(0, (int) counter.getAttribute(COUNTER).get());
    }

    private Configuration getCounterConfiguration(String target, int interval) {
        return new ConfigurationBuilder().addString(CounterUnit.KEY_TARGET, target)
                .addInteger(CounterUnit.KEY_INTERVAL, interval)
                .addInteger(CounterUnit.KEY_RECEIVED_MESSAGE, CounterUnit.DEFAULT_RECEIVED_MESSAGE)
                .build();
    }

    private static <T, R> R getAttributeOrTimeout(RoboReference<T> roboReference, AttributeDescriptor<R> attributeDescriptor) throws InterruptedException, ExecutionException, TimeoutException {
        var attribute = roboReference.getAttribute(attributeDescriptor).get(TIMEOUT_MIN, TimeUnit.MINUTES);
        if (attribute == null) {
            attribute = roboReference.getAttribute(attributeDescriptor).get(TIMEOUT_MIN, TimeUnit.MINUTES);
            LOGGER.error("roboReference:{}, no attribute:{}", roboReference.getId(), attributeDescriptor.getAttributeName());
        }
        return attribute;
    }

}
