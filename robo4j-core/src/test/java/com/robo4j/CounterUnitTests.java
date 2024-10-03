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

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for the CounterUnit.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class CounterUnitTests {
	private static final String ID_COUNTER = "counter";
	private static final String ID_CONSUMER = "consumer";
	private static final AttributeDescriptor<Integer> NUMBER_OF_MESSAGES = new DefaultAttributeDescriptor<>(Integer.class,
			"NumberOfReceivedMessages");
	private static final AttributeDescriptor<Integer> COUNTER = new DefaultAttributeDescriptor<>(Integer.class, "Counter");

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final AttributeDescriptor<ArrayList<Integer>> MESSAGES = new DefaultAttributeDescriptor<ArrayList<Integer>>(
			(Class<ArrayList<Integer>>) new ArrayList().getClass(), "ReceivedMessages");

	@Test
	void test() throws RoboBuilderException, InterruptedException, ExecutionException {
		// FIXME(Marcus/Aug 20, 2017): We really should get rid of the sleeps
		// here and use waits with timeouts...
		RoboBuilder builder = new RoboBuilder();
		builder.add(IntegerConsumer.class, ID_CONSUMER);
		builder.add(CounterUnit.class, getCounterConfiguration(ID_CONSUMER, 1000), ID_COUNTER);
		RoboContext context = builder.build();
		context.start();
		assertEquals(LifecycleState.STARTED, context.getState());
		RoboReference<CounterCommand> counter = context.getReference(ID_COUNTER);
		RoboReference<Integer> consumer = context.getReference(ID_CONSUMER);
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
		Configuration configuration = new ConfigurationBuilder().addString(CounterUnit.KEY_TARGET, target)
				.addInteger(CounterUnit.KEY_INTERVAL, interval).build();
		return configuration;
	}

}
