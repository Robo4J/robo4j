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
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.util.SystemUtil;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test(s) for the builder.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboBuilderTests {
	private static final int MESSAGES = 10;
	private static final int TIMEOUT = 100;

	@Test
	void testParsingFile()
			throws RoboBuilderException, InterruptedException, ExecutionException, TimeoutException {
		RoboBuilder builder = new RoboBuilder();
		builder.add(SystemUtil.getInputStreamByResourceName("test.xml"));
		RoboContext system = builder.build();

		assertEquals(system.getState(), LifecycleState.INITIALIZED);
		system.start();
		assertTrue(system.getState() == LifecycleState.STARTING || system.getState() == LifecycleState.STARTED);


		RoboReference<String> producer = system.getReference("producer");
		assertNotNull(producer);
		for (int i = 0; i < MESSAGES; i++) {
			producer.sendMessage("sendRandomMessage");
		}
		RoboReference<String> consumer = system.getReference("consumer");
		CountDownLatch countDownLatchConsumer = consumer.getAttribute(StringConsumer.DESCRIPTOR_COUNT_DOWN_LATCH)
				.get(TIMEOUT, TimeUnit.MINUTES);
		countDownLatchConsumer.await(TIMEOUT, TimeUnit.MINUTES);

		assertEquals(MESSAGES, (int) producer.getAttribute(StringProducer.DESCRIPTOR_TOTAL_MESSAGES).get());
		assertEquals(MESSAGES, (int) consumer.getAttribute(StringConsumer.DESCRIPTOR_TOTAL_MESSAGES).get());

		system.stop();
		system.shutdown();
	}

	@Test
	void testSeparateSystemUnitsSystemConfig()
			throws RoboBuilderException, InterruptedException, ExecutionException, TimeoutException {
		RoboBuilder builder = new RoboBuilder(SystemUtil.getInputStreamByResourceName("testRoboSystemOnly.xml"));
		// NOTE(Marcus/Aug 19, 2017): We have the system settings and the units
		// in the same file.
		builder.add(Thread.currentThread().getContextClassLoader().getResourceAsStream("testRoboUnitsOnly.xml"));
		RoboContext system = builder.build();
		assertEquals("mySystem", system.getId());
		assertEquals(system.getState(), LifecycleState.INITIALIZED);
		system.start();
		assertTrue(system.getState() == LifecycleState.STARTING || system.getState() == LifecycleState.STARTED);

		/* descriptor is similar for both units */
		final DefaultAttributeDescriptor<Integer> descriptor = DefaultAttributeDescriptor.create(Integer.class,
				"getNumberOfSentMessages");

		RoboReference<String> producer = system.getReference("producer");
		assertNotNull(producer);
		for (int i = 0; i < MESSAGES; i++) {
			producer.sendMessage("sendRandomMessage");
		}


		assertEquals(MESSAGES, (int) producer.getAttribute(descriptor).get());

		RoboReference<String> consumer = system.getReference("consumer");
		assertNotNull(consumer);


		/* wait until message are received */
		CountDownLatch countDownLatchConsumer = consumer.getAttribute(StringProducer.DESCRIPTOR_COUNT_DOWN_LATCH)
				.get(TIMEOUT, TimeUnit.MINUTES);
		countDownLatchConsumer.await(TIMEOUT, TimeUnit.MINUTES);

		assertEquals(MESSAGES, (int) producer.getAttribute(StringProducer.DESCRIPTOR_TOTAL_MESSAGES).get());
		assertEquals(MESSAGES, (int) consumer.getAttribute(StringConsumer.DESCRIPTOR_TOTAL_MESSAGES).get());

		system.stop();
		system.shutdown();
	}

	@Test
	void testParsingFileWithSystemConfig()
			throws RoboBuilderException, InterruptedException, ExecutionException, TimeoutException {
		RoboBuilder builder = new RoboBuilder(
				Thread.currentThread().getContextClassLoader().getResourceAsStream("testsystem.xml"));
		// NOTE(Marcus/Aug 19, 2017): We have the system settings and the units
		// in the same file, therefore we pass the same file to the unit
		// configuration.
		builder.add(Thread.currentThread().getContextClassLoader().getResourceAsStream("testsystem.xml"));
		RoboContext system = builder.build();
		assertEquals("mySystem", system.getId());
		assertEquals(system.getState(), LifecycleState.INITIALIZED);
		system.start();
		assertTrue(system.getState() == LifecycleState.STARTING || system.getState() == LifecycleState.STARTED);

		RoboReference<String> producer = system.getReference("producer");
		CountDownLatch producerLatch = producer.getAttribute(StringProducer.DESCRIPTOR_COUNT_DOWN_LATCH).get();
		assertNotNull(producer);
		for (int i = 0; i < MESSAGES; i++) {
			producer.sendMessage("sendRandomMessage");
		}
		producerLatch.await(20, TimeUnit.SECONDS);
		int totalMessages = producer.getAttribute(StringProducer.DESCRIPTOR_TOTAL_MESSAGES).get();
		assertEquals(MESSAGES, totalMessages);

		RoboReference<String> consumer = system.getReference("consumer");
		assertNotNull(consumer);

		// We need to fix these tests so that we can get a callback.
		CountDownLatch countDownLatchConsumer = consumer.getAttribute(StringConsumer.DESCRIPTOR_COUNT_DOWN_LATCH)
				.get(TIMEOUT, TimeUnit.MINUTES);
		countDownLatchConsumer.await(TIMEOUT, TimeUnit.MINUTES);

		int receivedMessages = consumer.getAttribute(StringConsumer.DESCRIPTOR_TOTAL_MESSAGES).get();
		assertEquals(MESSAGES, receivedMessages);

		system.stop();
		system.shutdown();
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
		RoboReference<Object> reference = system.getReference("consumer");
		system.stop();
		system.shutdown();
		assertNotNull(reference);
	}

	@Test
	void testProgrammaticConfiguration() throws RoboBuilderException, ConfigurationException,
			InterruptedException, ExecutionException, TimeoutException {

		final String producerUnitName = "producer";
		final String consumerUnitName = "consumer";
		final int numberOfMessages = 10;

		ConfigurationBuilder systemConfigBuilder = new ConfigurationBuilder()
				.addInteger(RoboBuilder.KEY_SCHEDULER_POOL_SIZE, 11).addInteger(RoboBuilder.KEY_WORKER_POOL_SIZE, 5)
				.addInteger(RoboBuilder.KEY_BLOCKING_POOL_SIZE, 13);
		RoboBuilder builder = new RoboBuilder("mySystem", systemConfigBuilder.build());

		final Configuration producerConf = new ConfigurationBuilder().addString(StringProducer.PROP_TARGET, consumerUnitName)
				.addInteger(StringProducer.PROP_TOTAL_MESSAGES, numberOfMessages).build();
		final Configuration consumerConf = new ConfigurationBuilder()
				.addInteger(StringConsumer.PROP_TOTAL_MESSAGES, numberOfMessages).build();

		builder.add(StringProducer.class, producerConf, producerUnitName);
		builder.add(StringConsumer.class, consumerConf, consumerUnitName);
		RoboContext system = builder.build();

		assertEquals("mySystem", system.getId());
		assertEquals(system.getState(), LifecycleState.INITIALIZED);
		system.start();
		assertTrue(system.getState() == LifecycleState.STARTING || system.getState() == LifecycleState.STARTED);

		RoboReference<String> producerRef = system.getReference(producerUnitName);
		assertNotNull(producerRef);
		for (int i = 0; i < numberOfMessages; i++) {
			producerRef.sendMessage(StringProducer.PROPERTY_SEND_RANDOM_MESSAGE);
		}
		final int producerTotalSentMessages = producerRef.getAttribute(StringProducer.DESCRIPTOR_TOTAL_MESSAGES).get();
		assertEquals(numberOfMessages, producerTotalSentMessages);

		RoboReference<String> consumer = system.getReference(consumerUnitName);
		assertNotNull(consumer);

		// We need to fix these tests so that we can get a callback.
		CountDownLatch countDownLatchConsumer = consumer.getAttribute(StringConsumer.DESCRIPTOR_COUNT_DOWN_LATCH)
				.get(TIMEOUT, TimeUnit.MINUTES);
		countDownLatchConsumer.await(TIMEOUT, TimeUnit.MINUTES);

		int receivedMessages = consumer.getAttribute(StringConsumer.DESCRIPTOR_TOTAL_MESSAGES).get();
		assertEquals(MESSAGES, receivedMessages);

		system.stop();
		system.shutdown();
	}
}
