/*
 * Copyright (c) 2014, 2017, Miroslav Wengner, Marcus Hirt
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

import com.robo4j.commons.enums.LifecycleState;
import com.robo4j.core.system.RoboSystem;
import org.junit.Assert;
import org.junit.Test;

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
		producer.initialize(TestToolkit.createSingleValueProps("target", "consumer"));
		StringConsumer consumer = new StringConsumer(system, "consumer");
		system.addUnits(producer, consumer);
		Assert.assertEquals(system.getState(), LifecycleState.UNINITIALIZED);
		system.start();
		Assert.assertTrue(system.getState() == LifecycleState.STARTING || system.getState() == LifecycleState.STARTED);
		for (int i = 0; i < 10; i++) {
			producer.sendRandomMessage();
		}
		producer.shutdown();
		Assert.assertEquals(10, consumer.getReceivedMessages().size());		
	}
}
