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
import com.robo4j.scheduler.FinalInvocationListener;
import com.robo4j.units.StringConsumer;
import com.robo4j.units.StringProducer;
import com.robo4j.units.StringScheduledEmitter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.robo4j.RoboUnitTestUtils.getAttributeOrTimeout;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testing scheduling messages.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class RoboSchedulerTests {
    private static class SchedulerListener implements FinalInvocationListener {
        volatile boolean wasFinalCalled;

        @Override
        public void onFinalInvocation(RoboContext context) {
            wasFinalCalled = true;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(RoboSchedulerTests.class);

    @Test
    void testScheduler() throws InterruptedException, ExecutionException, TimeoutException, RoboBuilderException, CancellationException {
        // FIXME: 20.08.17 (miro,marcus): when notification implemented, correct the test
        final var usedTimeUnitMills = TimeUnit.MILLISECONDS;
        final var maxDelayMills = 2;
        final var initSystemDelayMills = 1;
        final var scheduledMessageRepeatExclusive = 4;
        final var usedScheduledMessage = "Lalalala";
        final int expectedMessagesMax = 3;
        final var expectedConsumerName = "consumer";
        final Configuration consumerConf = new ConfigurationBuilder()
                .addInteger(StringConsumer.PROP_TOTAL_MESSAGES, expectedMessagesMax).build();

        var roboSystemBuilder = new RoboBuilder();
        roboSystemBuilder.add(StringConsumer.class, consumerConf, expectedConsumerName);
        var system = (RoboSystem) roboSystemBuilder.build();
        system.start();

        var consumerRef = system.getReference(expectedConsumerName);
        assert consumerRef != null;
        var scheduler = system.getScheduler();
        var consumerReference = system.getReference(expectedConsumerName);
        var countDownLatchConsumer = getAttributeOrTimeout(consumerRef, StringConsumer.DESCRIPTOR_COUNT_DOWN_LATCH);
        var listener = new SchedulerListener();
        var schedule = scheduler.schedule(consumerReference, usedScheduledMessage, initSystemDelayMills, scheduledMessageRepeatExclusive, usedTimeUnitMills, expectedMessagesMax, listener);
        try {
            schedule.get();
        } catch (CancellationException e) {
            LOGGER.info("expected exception due to the cancellation of scheduler,e:{}", String.valueOf(e));
        }

        var receivedMessages = countDownLatchConsumer.await(maxDelayMills, usedTimeUnitMills);
        var totalReceivedMessages = getAttributeOrTimeout(consumerRef, StringConsumer.DESCRIPTOR_TOTAL_MESSAGES);

        assertTrue(receivedMessages);
        assertEquals(expectedMessagesMax, totalReceivedMessages);
        assertTrue(listener.wasFinalCalled);
        system.shutdown();
    }

    @Test
    void testProducerWithSchedulerConsumer() throws ConfigurationException, InterruptedException {
        int totalMessages = 10;
        int consideredTimeoutMills = 10;
        int totalTestTimeoutMills = consideredTimeoutMills * totalMessages + consideredTimeoutMills;
        var system = new RoboSystem();
        var producer = new StringProducer(system, StringProducer.DEFAULT_UNIT_NAME);
        var producerConfig = new ConfigurationBuilder().addString(StringProducer.PROP_TARGET, StringScheduledEmitter.DEFAULT_UNIT_NAME)
                .addInteger(StringProducer.PROP_TOTAL_MESSAGES, totalMessages).build();
        producer.initialize(producerConfig);
        var producerCountDownLatch = producer.onGetAttribute(StringProducer.DESCRIPTOR_COUNT_DOWN_LATCH);

        var scheduledEmitter = new StringScheduledEmitter(system, StringScheduledEmitter.DEFAULT_UNIT_NAME);
        var scheduledEmitterConfig = new ConfigurationBuilder().addString(StringScheduledEmitter.PROP_TARGET, StringConsumer.DEFAULT_UNIT_NAME)
                .build();
        scheduledEmitter.initialize(scheduledEmitterConfig);

        var consumer = new StringConsumer(system, StringConsumer.DEFAULT_UNIT_NAME);
        var consumerConfig = new ConfigurationBuilder().addInteger(StringConsumer.PROP_TOTAL_MESSAGES, totalMessages).build();
        consumer.initialize(consumerConfig);
        var consumerCountDownLatch = consumer.onGetAttribute(StringConsumer.DESCRIPTOR_COUNT_DOWN_LATCH);

        system.addUnits(producer, scheduledEmitter, consumer);
        system.setState(LifecycleState.INITIALIZED);
        system.start();

        for (int i = 0; i < totalMessages; i++) {
            producer.onMessage(StringProducer.PROPERTY_SEND_RANDOM_MESSAGE);
        }

        var producerSentMessages = producerCountDownLatch.await(totalTestTimeoutMills, TimeUnit.MILLISECONDS);
        var consumerReceivedMessages = consumerCountDownLatch.await(totalTestTimeoutMills, TimeUnit.MILLISECONDS);
        var producerTotalMessagesCount = producer.onGetAttribute(StringProducer.DESCRIPTOR_TOTAL_MESSAGES);

        system.shutdown();

        assertTrue(producerSentMessages);
        assertEquals(totalMessages, producerTotalMessagesCount);
        assertTrue(consumerReceivedMessages);
        assert (totalMessages <= consumer.getReceivedMessages().size());
    }

    @Test
    void schedulerWithPressureAndMultipleTasksTest() throws InterruptedException, ExecutionException {
        RoboSystem system = new RoboSystem();
        StringConsumer consumer = new StringConsumer(system, "consumer");
        system.addUnits(consumer);

        SchedulerListener listener = new SchedulerListener();
        ScheduledFuture<?> f1 = system.getScheduler().schedule(consumer, "Lalalala", 0, 2, TimeUnit.MILLISECONDS, 1500,
                listener);
        ScheduledFuture<?> f2 = system.getScheduler().schedule(consumer, "bläblä", 1, 2, TimeUnit.MILLISECONDS, 1500);

        get(f1);
        get(f2);

        assertEquals(3000, consumer.getReceivedMessages().size());
        assertTrue(listener.wasFinalCalled);
        system.shutdown();
    }

    private void get(ScheduledFuture<?> f) throws InterruptedException, ExecutionException {
        try {
            f.get();
        } catch (Throwable e) {
            // Expected - using this to wait for completion.
        }
    }
}
