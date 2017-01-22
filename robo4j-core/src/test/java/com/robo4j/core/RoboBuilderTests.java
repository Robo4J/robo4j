/*
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
import com.robo4j.commons.io.RoboContext;
import com.robo4j.commons.unit.RoboUnit;
import com.robo4j.core.client.util.ClientClassLoader;
import com.robo4j.core.system.RoboBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test(s) for the builder.
 *  
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboBuilderTests {

	@Test
	public void testParsingFile() throws RoboBuilderException {
		RoboBuilder builder = new RoboBuilder();
		builder.add(ClientClassLoader.getInstance().getResource("test.xml"));
//		builder.add(RoboBuilderTests.class.getResourceAsStream("test.xml"));
		RoboContext context = builder.build();
		Assert.assertEquals(context.getState(), LifecycleState.UNINITIALIZED);
		context.start();
		Assert.assertTrue(context.getState() == LifecycleState.STARTING || context.getState() == LifecycleState.STARTED);
		
		RoboUnit<?> roboUnit = context.getRoboUnit("producer");
		Assert.assertTrue(roboUnit instanceof StringProducer);
		
		StringProducer producer = (StringProducer) roboUnit;
		
		roboUnit = context.getRoboUnit("consumer");		
		Assert.assertTrue(roboUnit instanceof StringConsumer);
		StringConsumer consumer = (StringConsumer) roboUnit; 
		
		for (int i = 0; i < 10; i++) {
			producer.sendRandomMessage();
		}
		producer.shutdown();
		Assert.assertEquals(10, consumer.getReceivedMessages().size());		

	}

	@Test
	public void testAddingNonUnique() {
		RoboBuilder builder = new RoboBuilder();
		boolean gotException = false;
		try {
			builder.add(ClientClassLoader.getInstance().getResource("double.xml"));
		} catch (RoboBuilderException e) {
			gotException = true;
		}
		Assert.assertTrue(gotException);
	}


}
