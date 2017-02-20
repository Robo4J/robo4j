/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This RoboHttpDynamicTests.java  is part of robo4j.
 * module: robo4j-core
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.unit;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboSystem;
import com.robo4j.core.StringConsumer;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.core.util.SystemUtil;

/**
 *
 * Dynamic HttpUnit request/method configuration
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboHttpDynamicTests {

	private static final int PORT = 8025;

	@Test
	public void simpleHttpNonUnitTest() throws ConfigurationException, IOException {
		RoboSystem system = new RoboSystem();
		Configuration config = ConfigurationFactory.createEmptyConfiguration();

		HttpServerUnit httpDynamic = new HttpServerUnit(system, "http");
		config.setString("target", "request_consumer");
		config.setInteger("port", PORT);
		httpDynamic.initialize(config);
		//TODO FIXME: implement some logic how to


		StringConsumer consumer = new StringConsumer(system, "request_consumer");

		Assert.assertNotNull(system.getUnits());
		Assert.assertEquals(system.getUnits().size(), 0);
		Assert.assertEquals(httpDynamic.getState(), LifecycleState.INITIALIZED);
		Assert.assertEquals(system.getState(), LifecycleState.UNINITIALIZED);

		system.addUnits(httpDynamic, consumer);

		System.out.println("State before start:");
		System.out.println(SystemUtil.generateStateReport(system));
		system.start();

		System.out.println("State after start:");
		System.out.println(SystemUtil.generateStateReport(system));

		System.out.println("RoboSystem http server\n\tPort:" + PORT + "\n");
		System.out.println("Usage:\n\tRequest GET: http://<IP_ADDRESS>:" + PORT + "/tank?command=move");
		System.out.println("\tRequest command types: right,left,move,back,enter\n");

		System.in.read();

		System.out.println("Going Down!");
		system.stop();
		system.shutdown();
		System.out.println("System is Down!");
		Assert.assertNotNull(system.getUnits());
		Assert.assertEquals(system.getUnits().size(), 2);
		Assert.assertEquals(consumer.getReceivedMessages().size(), 0);
	}
}
