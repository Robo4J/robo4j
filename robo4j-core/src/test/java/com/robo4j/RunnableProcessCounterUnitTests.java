/*
 * Copyright (c) 2014, 2025, Marcus Hirt, Miroslav Wengner
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.units.CounterCommand;
import com.robo4j.units.CounterUnit;
import com.robo4j.units.IntegerConsumer;

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
	private static final AttributeDescriptor<Integer> NUMBER_OF_MESSAGES = new DefaultAttributeDescriptor<>(Integer.class, "NumberOfReceivedMessages");

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
		var updatedMessageOffsetAfterStop = CounterUnit.DEFAULT_RECEIVED_MESSAGE + 2;
		var counterMessageProducerInterval = 1000;
		RoboBuilder builder = new RoboBuilder();
		builder.add(IntegerConsumer.class, CONSUMER_ID);
		builder.add(CounterUnit.class, getCounterConfiguration(CONSUMER_ID, counterMessageProducerInterval), COUNTER_PRODUCER_ID);
		RoboContext context = builder.build();
		context.start();

		RoboReference<CounterCommand> messageProducer = context.getReference(COUNTER_PRODUCER_ID);
		messageProducer.sendMessage(CounterCommand.START);
		var latchCreatedMessagesInInterval = getAttributeOrTimeout(messageProducer, CounterUnit.DESCRIPTOR_REPORT_RECEIVED_MESSAGES_LATCH);
		var unitActiveInternalProcessDone = getAttributeOrTimeout(messageProducer, CounterUnit.DESCRIPTOR_PROCESS_DONE);
		var createdMessagesAfterStart = latchCreatedMessagesInInterval.await(TIMEOUT_MIN, TimeUnit.MINUTES);
		messageProducer.sendMessage(CounterCommand.STOP);
		messageProducer.sendMessage(CounterCommand.COUNTER_INC);
		messageProducer.sendMessage(CounterCommand.COUNTER_INC);
		messageProducer.sendMessage(CounterCommand.RESET);
		var latchCreatedMessagesAfterStop = getAttributeOrTimeout(messageProducer, CounterUnit.DESCRIPTOR_REPORT_RECEIVED_MESSAGES_LATCH);

		var activeAfterStopLatch = latchCreatedMessagesAfterStop.await(TIMEOUT_MIN, TimeUnit.MINUTES);
		var updatedReceivedMessageOffset = getAttributeOrTimeout(messageProducer, CounterUnit.DESCRIPTOR_RECEIVED_MESSAGE_OFFSET);
		var internalProcessDone = getAttributeOrTimeout(messageProducer, CounterUnit.DESCRIPTOR_PROCESS_DONE);

		RoboReference<Integer> consumer = context.getReference(CONSUMER_ID);
		var receivedMessages = getAttributeOrTimeout(consumer, NUMBER_OF_MESSAGES);

		assertFalse(unitActiveInternalProcessDone);
		assertTrue(createdMessagesAfterStart);
		assertEquals(LifecycleState.STARTED, context.getState());
		assertTrue(activeAfterStopLatch);
		assertTrue(internalProcessDone);
		assertEquals(CounterUnit.DEFAULT_RECEIVED_MESSAGE, receivedMessages);
		assertEquals(updatedMessageOffsetAfterStop, updatedReceivedMessageOffset);
	}

	private Configuration getCounterConfiguration(String target, int interval) {
		return new ConfigurationBuilder().addString(CounterUnit.KEY_TARGET, target).addInteger(CounterUnit.KEY_INTERVAL, interval)
				.addInteger(CounterUnit.KEY_RECEIVED_MESSAGE, CounterUnit.DEFAULT_RECEIVED_MESSAGE).build();
	}

	private static <T, R> R getAttributeOrTimeout(RoboReference<T> roboReference, AttributeDescriptor<R> attributeDescriptor)
			throws InterruptedException, ExecutionException, TimeoutException {
		var attribute = roboReference.getAttribute(attributeDescriptor).get(TIMEOUT_MIN, TimeUnit.MINUTES);
		if (attribute == null) {
			attribute = roboReference.getAttribute(attributeDescriptor).get(TIMEOUT_MIN, TimeUnit.MINUTES);
			LOGGER.error("roboReference:{}, no attribute:{}", roboReference.id(), attributeDescriptor.attributeName());
		}
		return attribute;
	}

}
