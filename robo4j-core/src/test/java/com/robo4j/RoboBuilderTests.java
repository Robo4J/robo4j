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
import com.robo4j.units.StringConsumer;
import com.robo4j.units.StringProducer;
import com.robo4j.util.SystemUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test(s) for the builder.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboBuilderTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoboBuilderTests.class);
    private static final int MESSAGES = 10;
    private static final int TIMEOUT_MIN = 2;
    private static final String SYSTEM_CONFIG_NAME = "mySystem";
    private static final String PRODUCER_UNIT_NAME = "producer";
    private static final String CONSUMER_UNIT_NAME = "consumer";


    @Test
    void testParsingFileWithSystemConfigAndInitiate() throws RoboBuilderException {
        var fileSystemAndUnitsConfigName = "testsystem.xml";
        var contextLoader = Thread.currentThread().getContextClassLoader();
        RoboBuilder builder = new RoboBuilder(
                contextLoader.getResourceAsStream(fileSystemAndUnitsConfigName));
        builder.add(contextLoader.getResourceAsStream(fileSystemAndUnitsConfigName));
        RoboContext system = builder.build();
        assertEquals(SYSTEM_CONFIG_NAME, system.getId());
        assertEquals(system.getState(), LifecycleState.INITIALIZED);
    }

    @Test
    void testParsingFileWithSystemConfigAndStart() throws RoboBuilderException {
        var fileSystemAndUnitsConfigName = "testsystem.xml";
        var contextLoader = Thread.currentThread().getContextClassLoader();
        RoboBuilder builder = new RoboBuilder(
                contextLoader.getResourceAsStream(fileSystemAndUnitsConfigName));
        builder.add(contextLoader.getResourceAsStream(fileSystemAndUnitsConfigName));
        RoboContext system = builder.build();
        system.start();
        assertSame(system.getState(), LifecycleState.STARTED);
    }


    /**
     * Configuration of the system and units resides inside the same file, testsystem.xml
     */
    @Test
    void testParsingFileWithSystemConfig()
            throws RoboBuilderException, InterruptedException, ExecutionException, TimeoutException {
        var fileSystemAndUnitsConfigName = "testsystem.xml";
        var contextLoader = Thread.currentThread().getContextClassLoader();
        RoboBuilder builder = new RoboBuilder(
                contextLoader.getResourceAsStream(fileSystemAndUnitsConfigName));
        builder.add(contextLoader.getResourceAsStream(fileSystemAndUnitsConfigName));
        RoboContext system = builder.build();
        system.start();
        RoboReference<String> producer = system.getReference(PRODUCER_UNIT_NAME);
        CountDownLatch producerLatch = getAttributeOrTimeout(producer, StringProducer.DESCRIPTOR_COUNT_DOWN_LATCH);

        for (int i = 0; i < MESSAGES; i++) {
            producer.sendMessage("sendRandomMessage");
        }
        var messagesProduced = producerLatch.await(TIMEOUT_MIN, TimeUnit.MINUTES);
        var totalProducedMessages = getAttributeOrTimeout(producer, StringProducer.DESCRIPTOR_TOTAL_MESSAGES);

        RoboReference<String> consumer = system.getReference(CONSUMER_UNIT_NAME);
        CountDownLatch countDownLatchConsumer = getAttributeOrTimeout(consumer, StringConsumer.DESCRIPTOR_COUNT_DOWN_LATCH);
        var messageReceived = countDownLatchConsumer.await(TIMEOUT_MIN, TimeUnit.MINUTES);
        var totalReceivedMessages = consumer.getAttribute(StringConsumer.DESCRIPTOR_TOTAL_MESSAGES).get();

        system.stop();
        system.shutdown();

        assertNotNull(producer);
        assertNotNull(consumer);
        assertTrue(messagesProduced);
        assertTrue(messageReceived);
        assertEquals(MESSAGES, totalProducedMessages);
        assertEquals(MESSAGES, totalReceivedMessages);
    }

    @Test
    void testParsingFile()
            throws RoboBuilderException, InterruptedException, ExecutionException, TimeoutException {
        RoboBuilder builder = new RoboBuilder();
        builder.add(SystemUtil.getInputStreamByResourceName("test.xml"));
        RoboContext system = builder.build();
        system.start();

        RoboReference<String> producer = system.getReference(PRODUCER_UNIT_NAME);
        for (int i = 0; i < MESSAGES; i++) {
            producer.sendMessage("sendRandomMessage");
        }
        var totalProducedMessages = getAttributeOrTimeout(producer, StringProducer.DESCRIPTOR_TOTAL_MESSAGES);

        RoboReference<String> consumer = system.getReference(CONSUMER_UNIT_NAME);

        CountDownLatch countDownLatchConsumer = getAttributeOrTimeout(consumer, StringConsumer.DESCRIPTOR_COUNT_DOWN_LATCH);
        var receivedMessages = countDownLatchConsumer.await(TIMEOUT_MIN, TimeUnit.MINUTES);
        var totalReceivedMessages = getAttributeOrTimeout(consumer, StringConsumer.DESCRIPTOR_TOTAL_MESSAGES);

        system.stop();
        system.shutdown();

        assertNotNull(producer);
        assertTrue(receivedMessages);
        assertEquals(MESSAGES, totalProducedMessages);
        assertEquals(MESSAGES, totalReceivedMessages);
    }


    @Test
    void testSeparateSystemUnitsSystemConfig()
            throws RoboBuilderException, InterruptedException, ExecutionException, TimeoutException {
        RoboBuilder builder = new RoboBuilder(SystemUtil.getInputStreamByResourceName("testRoboSystemOnly.xml"));
        builder.add(Thread.currentThread().getContextClassLoader().getResourceAsStream("testRoboUnitsOnly.xml"));
        RoboContext system = builder.build();

        system.start();

        RoboReference<String> producer = system.getReference(PRODUCER_UNIT_NAME);
        CountDownLatch producerLatch = getAttributeOrTimeout(producer, StringProducer.DESCRIPTOR_COUNT_DOWN_LATCH);
        for (int i = 0; i < MESSAGES; i++) {
            producer.sendMessage("sendRandomMessage");
        }
        var messagesProduced = producerLatch.await(TIMEOUT_MIN, TimeUnit.MINUTES);
        var totalProducedMessages = producer.getAttribute(StringProducer.DESCRIPTOR_TOTAL_MESSAGES).get();

        RoboReference<String> consumer = system.getReference(CONSUMER_UNIT_NAME);

        CountDownLatch countDownLatchConsumer = getAttributeOrTimeout(consumer, StringProducer.DESCRIPTOR_COUNT_DOWN_LATCH);
        var receivedMessages = countDownLatchConsumer.await(TIMEOUT_MIN, TimeUnit.MINUTES);
        var totalReceivedMessages = getAttributeOrTimeout(consumer, StringConsumer.DESCRIPTOR_TOTAL_MESSAGES);

        system.stop();
        system.shutdown();

        assertEquals(SYSTEM_CONFIG_NAME, system.getId());
        assertNotNull(producer);
        assertNotNull(consumer);
        assertTrue(messagesProduced);
        assertTrue(receivedMessages);
        assertEquals(MESSAGES, totalProducedMessages);
        assertEquals(MESSAGES, totalReceivedMessages);
    }


    @Test
    void testAddingNonUnique() {
        RoboBuilder builder = new RoboBuilder();
        boolean gotException = false;
        try {
            builder.add(Thread.currentThread().getContextClassLoader().getResourceAsStream("double.xml"));
        } catch (RoboBuilderException e) {
            gotException = true;
        }
        assertTrue(gotException);
    }

    @Test
    void testComplexConfiguration() throws RoboBuilderException {
        RoboBuilder builder = new RoboBuilder();
        builder.add(Thread.currentThread().getContextClassLoader().getResourceAsStream("testsubconfig.xml"));
        RoboContext system = builder.build();
        system.start();

        RoboReference<Object> reference = system.getReference(CONSUMER_UNIT_NAME);

        system.stop();
        system.shutdown();

        assertNotNull(reference);
    }

    @Test
    void testProgrammaticConfiguration() throws RoboBuilderException, InterruptedException, ExecutionException, TimeoutException {
        ConfigurationBuilder systemConfigBuilder = new ConfigurationBuilder()
                .addInteger(RoboBuilder.KEY_SCHEDULER_POOL_SIZE, 11).addInteger(RoboBuilder.KEY_WORKER_POOL_SIZE, 5)
                .addInteger(RoboBuilder.KEY_BLOCKING_POOL_SIZE, 13);
        RoboBuilder builder = new RoboBuilder(SYSTEM_CONFIG_NAME, systemConfigBuilder.build());

        final Configuration producerConf = new ConfigurationBuilder().addString(StringProducer.PROP_TARGET, CONSUMER_UNIT_NAME)
                .addInteger(StringProducer.PROP_TOTAL_MESSAGES, MESSAGES).build();
        final Configuration consumerConf = new ConfigurationBuilder()
                .addInteger(StringConsumer.PROP_TOTAL_MESSAGES, MESSAGES).build();

        builder.add(StringProducer.class, producerConf, PRODUCER_UNIT_NAME);
        builder.add(StringConsumer.class, consumerConf, CONSUMER_UNIT_NAME);
        RoboContext system = builder.build();
        system.start();

        RoboReference<String> producer = system.getReference(PRODUCER_UNIT_NAME);
        CountDownLatch producerLatch = getAttributeOrTimeout(producer, StringProducer.DESCRIPTOR_COUNT_DOWN_LATCH);

        for (int i = 0; i < MESSAGES; i++) {
            producer.sendMessage(StringProducer.PROPERTY_SEND_RANDOM_MESSAGE);
        }
        var producedMessage = producerLatch.await(TIMEOUT_MIN, TimeUnit.MINUTES);
        var totalProducedMessages = producer.getAttribute(StringProducer.DESCRIPTOR_TOTAL_MESSAGES).get();


        RoboReference<String> consumer = system.getReference(CONSUMER_UNIT_NAME);
        // We need to fix these tests so that we can get a callback.
        CountDownLatch countDownLatchConsumer = getAttributeOrTimeout(consumer, StringConsumer.DESCRIPTOR_COUNT_DOWN_LATCH);
        var messageReceived = countDownLatchConsumer.await(TIMEOUT_MIN, TimeUnit.MINUTES);

        int totalReceivedMessages = getAttributeOrTimeout(consumer, StringConsumer.DESCRIPTOR_TOTAL_MESSAGES);

        assertNotNull(producer);
        assertNotNull(consumer);
        assertTrue(producedMessage);
        assertTrue(messageReceived);
        assertEquals(MESSAGES, totalProducedMessages);
        assertEquals(SYSTEM_CONFIG_NAME, system.getId());
        assertEquals(MESSAGES, totalReceivedMessages);

        system.stop();
        system.shutdown();
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
