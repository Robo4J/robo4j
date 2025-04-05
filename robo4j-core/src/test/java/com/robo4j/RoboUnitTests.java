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

import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.units.StringConsumer;
import com.robo4j.units.StringProducer;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static com.robo4j.RoboUnitTestUtils.getAttributeOrTimeout;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test(s) for the RoboUnits.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class RoboUnitTests {

    @Test
    void systemUninitializedTest() {
        RoboSystem system = new RoboSystem();
        assertEquals(LifecycleState.UNINITIALIZED, system.getState());
    }

    @Test
    void systemInitializedTest() {
        var system = new RoboSystem();
        var consumer = new StringConsumer(system, "consumer");
        system.addUnits(consumer);
        system.setState(LifecycleState.INITIALIZED);
        system.start();

        assertEquals(LifecycleState.STARTED, system.getState());
        system.shutdown();
    }

    @Test
    void systemStartTest() throws Exception {
        var consideredTimeUnit = TimeUnit.MILLISECONDS;
        var totalTestTimeoutMills = 10;
        int expectedTotalMessages = 10;
        var system = new RoboSystem();
        var producer = new StringProducer(system, "producer");
        var producerConfig = new ConfigurationBuilder().addString(StringProducer.PROP_TARGET, "consumer")
                .addInteger(StringProducer.PROP_TOTAL_MESSAGES, expectedTotalMessages)
                .build();
        producer.initialize(producerConfig);
        var consumer = new StringConsumer(system, "consumer");
        var consumerConfig = new ConfigurationBuilder().addInteger(StringConsumer.PROP_TOTAL_MESSAGES, expectedTotalMessages).build();
        consumer.initialize(consumerConfig);

        system.addUnits(producer, consumer);
        system.setState(LifecycleState.INITIALIZED);
        system.start();

        var countDownLatchConsumer = getAttributeOrTimeout(consumer, StringConsumer.DESCRIPTOR_COUNT_DOWN_LATCH);

        for (int i = 0; i < expectedTotalMessages; i++) {
            producer.sendRandomMessage();
        }
        var consumerReceivedMessages = countDownLatchConsumer.await(totalTestTimeoutMills, consideredTimeUnit);
        var totalReceivedMessages = getAttributeOrTimeout(consumer, StringConsumer.DESCRIPTOR_TOTAL_MESSAGES);

        assertTrue(consumerReceivedMessages);
        assertEquals(LifecycleState.STARTED, system.getState());
        assertEquals(expectedTotalMessages, totalReceivedMessages);
        system.shutdown();

    }

    @Test
    void systemStartedReferenceMessagesTest() throws Exception {
        var consideredTimeUnit = TimeUnit.MILLISECONDS;
        var totalTestTimeoutMills = 10;
        var randomMessage = "Lalalala";
        var expectedTotalMessages = 2;
        var system = new RoboSystem();
        var consumer = new StringConsumer(system, "consumer");
        var consumerConfig = new ConfigurationBuilder().addInteger(StringConsumer.PROP_TOTAL_MESSAGES, expectedTotalMessages).build();
        consumer.initialize(consumerConfig);
        system.addUnits(consumer);
        system.setState(LifecycleState.INITIALIZED);
        system.start();

        var consumerRef = system.getReference(consumer.id());
        assert consumerRef != null;

        var countDownLatchConsumer = getAttributeOrTimeout(consumer, StringConsumer.DESCRIPTOR_COUNT_DOWN_LATCH);
        consumer.sendMessage(randomMessage);
        consumerRef.sendMessage(randomMessage);


        var consumerReceivedMessages = countDownLatchConsumer.await(totalTestTimeoutMills, consideredTimeUnit);
        var totalReceivedMessages = getAttributeOrTimeout(consumer, StringConsumer.DESCRIPTOR_TOTAL_MESSAGES);
        system.shutdown();

        assertTrue(consumerReceivedMessages);
        assertEquals(expectedTotalMessages, totalReceivedMessages);
    }
}
