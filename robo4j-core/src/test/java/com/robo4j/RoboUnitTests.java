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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test(s) for the RoboUnits.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class RoboUnitTests {

	@Test
	void systemUninitializedTest(){
		RoboSystem system = new RoboSystem();
		assertEquals(system.getState(), LifecycleState.UNINITIALIZED);
	}

	@Test
	void systemInitializedTest(){
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
		int totalMessages = 10;
		var system = new RoboSystem();
		var producer = new StringProducer(system, "producer");
		var config = new ConfigurationBuilder().addString(StringProducer.PROP_TARGET, "consumer")
				.addInteger(StringProducer.PROP_TOTAL_MESSAGES, totalMessages)
				.build();
		producer.initialize(config);
		var consumer = new StringConsumer(system, "consumer");
		system.addUnits(producer, consumer);
		system.setState(LifecycleState.INITIALIZED);
		system.start();
		for (int i = 0; i < totalMessages; i++) {
			producer.sendRandomMessage();
		}
        Thread.sleep(10);

		assertEquals(system.getState(), LifecycleState.STARTED);
		assertEquals(totalMessages, consumer.getReceivedMessages().size());
		system.shutdown();
	}

	@Test
	void systemStartedReferenceMessagesTest() throws Exception {
		var system = new RoboSystem();
		var consumer = new StringConsumer(system, "consumer");
		system.addUnits(consumer);
		system.setState(LifecycleState.INITIALIZED);
		system.start();

		RoboReference<String> ref = system.getReference(consumer.id());
		assert ref != null;
		consumer.sendMessage("Lalalala");
		ref.sendMessage("Lalala");
		system.shutdown();
		assertEquals(2, consumer.getReceivedMessages().size());
	}
}
