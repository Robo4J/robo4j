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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.core;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.core.client.util.RoboClassLoader;

/**
 * Test(s) for the builder.
 *  
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboBuilderTests {
	private static final int MESSAGES = 10;

	@Test
	public void testParsingFile() throws RoboBuilderException, InterruptedException, ExecutionException {
		RoboBuilder builder = new RoboBuilder();
		builder.add(RoboClassLoader.getInstance().getResource("test.xml"));
		RoboContext context = builder.build();
		Assert.assertEquals(context.getState(), LifecycleState.UNINITIALIZED);
		context.start();
		Assert.assertTrue(context.getState() == LifecycleState.STARTING || context.getState() == LifecycleState.STARTED);
		
		RoboReference<String> producer = context.getReference("producer");
		Assert.assertNotNull(producer);

		RoboReference<String> consumer = context.getReference("consumer");		
		Assert.assertNotNull(consumer);

		for (int i = 0; i < MESSAGES; i++) {
			producer.sendMessage("sendRandomMessage");
		}

		DefaultAttributeDescriptor<Integer> descriptor = DefaultAttributeDescriptor
				.create(Integer.class, "getNumberOfSentMessages");
		Future<Integer> result = consumer.getAttribute(descriptor);
		context.shutdown();
		Assert.assertEquals(MESSAGES, (int) result.get());
	}

	@Test
	public void testAddingNonUnique() {
		RoboBuilder builder = new RoboBuilder();
		boolean gotException = false;
		try {
			builder.add(RoboClassLoader.getInstance().getResource("double.xml"));
		} catch (RoboBuilderException e) {
			gotException = true;
		}
		Assert.assertTrue(gotException);
	}


}
