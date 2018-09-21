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

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;

/**
 * Test(s) for the RoboUnits.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboUnitTests {

	@Test
	public void testSystem() throws Exception {
		RoboSystem system = new RoboSystem();
		Assert.assertEquals(system.getState(), LifecycleState.UNINITIALIZED);
		StringProducer producer = new StringProducer(system, "producer");
		Configuration config = new ConfigurationBuilder().addString("target", "consumer").build();
		producer.initialize(config);
		StringConsumer consumer = new StringConsumer(system, "consumer");
		system.addUnits(producer, consumer);
		system.start();
		Assert.assertEquals(system.getState(), LifecycleState.STARTED);
		Assert.assertTrue(system.getState() == LifecycleState.STARTING || system.getState() == LifecycleState.STARTED);
		for (int i = 0; i < 10; i++) {
			producer.sendRandomMessage();
		}
		system.shutdown();
		Assert.assertEquals(10, consumer.getReceivedMessages().size());
	}

	@Test
	public void testReferences() throws Exception {
		RoboSystem system = new RoboSystem();
		Assert.assertEquals(system.getState(), LifecycleState.UNINITIALIZED);
		StringConsumer consumer = new StringConsumer(system, "consumer");
		system.addUnits(consumer);
		Assert.assertEquals(system.getState(), LifecycleState.UNINITIALIZED);
		system.setState(LifecycleState.INITIALIZED);
		system.start();

		Assert.assertTrue(system.getState() == LifecycleState.STARTING || system.getState() == LifecycleState.STARTED);

		RoboReference<String> ref = system.getReference(consumer.getId());
		consumer.sendMessage("Lalalala");
		ref.sendMessage("Lalala");
		system.shutdown();
		Assert.assertEquals(2, consumer.getReceivedMessages().size());
	}
}
