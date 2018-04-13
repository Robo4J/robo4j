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

import com.robo4j.util.SystemUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Test(s) for the builder.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboBuilderTests {
	private static final int MESSAGES = 10;
	public static final int TIMEOUT = 100;

	@Test
	public void testParsingFile() throws RoboBuilderException, InterruptedException, ExecutionException, TimeoutException {
		RoboBuilder builder = new RoboBuilder();
		builder.add(SystemUtil.getInputStreamByResourceName("test.xml"));
		RoboContext system = builder.build();

		Assert.assertEquals(system.getState(), LifecycleState.INITIALIZED);
		system.start();
		Assert.assertTrue(system.getState() == LifecycleState.STARTING || system.getState() == LifecycleState.STARTED);

		/* descriptor is similar for both units */
		final DefaultAttributeDescriptor<Integer> descriptor = DefaultAttributeDescriptor.create(Integer.class,
				"getNumberOfSentMessages");

		RoboReference<String> producer = system.getReference("producer");
		Assert.assertNotNull(producer);
		for (int i = 0; i < MESSAGES; i++) {
			producer.sendMessage("sendRandomMessage");
		}
		RoboReference<String> consumer = system.getReference("consumer");
		CountDownLatch countDownLatchConsumer = consumer
				.getAttribute(StringConsumer.DESCRIPTOR_COUNT_DOWN_LATCH).get(TIMEOUT, TimeUnit.MINUTES);
		countDownLatchConsumer.await(TIMEOUT, TimeUnit.MINUTES);

		Assert.assertEquals(MESSAGES, (int) producer.getAttribute(descriptor).get());
		Assert.assertEquals(MESSAGES, (int) consumer.getAttribute(descriptor).get());

		system.stop();
		system.shutdown();
	}

//	@Test
	public void testSeparateSystemUnitsSystemConfig()
			throws RoboBuilderException, InterruptedException, ExecutionException {
		RoboBuilder builder = new RoboBuilder(SystemUtil.getInputStreamByResourceName("testRoboSystemOnly.xml"));
		// NOTE(Marcus/Aug 19, 2017): We have the system settings and the units
		// in the same file.
		builder.add(Thread.currentThread().getContextClassLoader().getResourceAsStream("testRoboUnitsOnly.xml"));
		RoboContext system = builder.build();
		Assert.assertEquals("mySystem", system.getId());
		Assert.assertEquals(system.getState(), LifecycleState.UNINITIALIZED);
		system.start();
		Assert.assertTrue(system.getState() == LifecycleState.STARTING || system.getState() == LifecycleState.STARTED);

		/* descriptor is similar for both units */
		final DefaultAttributeDescriptor<Integer> descriptor = DefaultAttributeDescriptor.create(Integer.class,
				"getNumberOfSentMessages");

		RoboReference<String> producer = system.getReference("producer");
		Assert.assertNotNull(producer);
		for (int i = 0; i < MESSAGES; i++) {
			producer.sendMessage("sendRandomMessage");
		}
		Assert.assertEquals(MESSAGES, (int) producer.getAttribute(descriptor).get());

		RoboReference<String> consumer = system.getReference("consumer");
		Assert.assertNotNull(consumer);

		synchronized (consumer.getAttribute(descriptor)) {
			int receivedMessages = consumer.getAttribute(descriptor).get();
			Assert.assertEquals(MESSAGES, receivedMessages);
		}

		system.stop();
		system.shutdown();
	}

	@Test
	public void testParsingFileWithSystemConfig()
			throws RoboBuilderException, InterruptedException, ExecutionException, TimeoutException {
		RoboBuilder builder = new RoboBuilder(
				Thread.currentThread().getContextClassLoader().getResourceAsStream("testsystem.xml"));
		// NOTE(Marcus/Aug 19, 2017): We have the system settings and the units
		// in the same file, therefore we pass the same file to the unit configuration.
		builder.add(Thread.currentThread().getContextClassLoader().getResourceAsStream("testsystem.xml"));
		RoboContext system = builder.build();
		Assert.assertEquals("mySystem", system.getId());
		Assert.assertEquals(system.getState(), LifecycleState.INITIALIZED);
		system.start();
		Assert.assertTrue(system.getState() == LifecycleState.STARTING || system.getState() == LifecycleState.STARTED);

		/* descriptor is similar for both units */
		final DefaultAttributeDescriptor<Integer> descriptor = DefaultAttributeDescriptor.create(Integer.class,
				"getNumberOfSentMessages");

		RoboReference<String> producer = system.getReference("producer");
		Assert.assertNotNull(producer);
		for (int i = 0; i < MESSAGES; i++) {
			producer.sendMessage("sendRandomMessage");
		}
		Assert.assertEquals(MESSAGES, (int) producer.getAttribute(descriptor).get());

		RoboReference<String> consumer = system.getReference("consumer");
		Assert.assertNotNull(consumer);

		// We need to fix these tests so that we can get a callback.
		CountDownLatch countDownLatchConsumer = consumer
				.getAttribute(StringConsumer.DESCRIPTOR_COUNT_DOWN_LATCH).get(TIMEOUT, TimeUnit.MINUTES);
		countDownLatchConsumer.await(TIMEOUT, TimeUnit.MINUTES);

		synchronized (consumer.getAttribute(descriptor)) {
			int receivedMessages = consumer.getAttribute(descriptor).get();
			Assert.assertEquals(MESSAGES, receivedMessages);
		}

		system.stop();
		system.shutdown();
	}

	@Test
	public void testAddingNonUnique() {
		RoboBuilder builder = new RoboBuilder();
		boolean gotException = false;
		try {
			builder.add(Thread.currentThread().getContextClassLoader().getResourceAsStream("double.xml"));
		} catch (RoboBuilderException e) {
			gotException = true;
		}
		Assert.assertTrue(gotException);
	}

	@Test
	public void testComplexConfiguration() throws RoboBuilderException {
		RoboBuilder builder = new RoboBuilder();
		builder.add(Thread.currentThread().getContextClassLoader().getResourceAsStream("testsubconfig.xml"));
		RoboContext system = builder.build();
		system.start();
		RoboReference<Object> reference = system.getReference("consumer");
		system.stop();
		system.shutdown();
		Assert.assertNotNull(reference);
	}

}
