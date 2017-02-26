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

package com.robo4j.core.httpunit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.robo4j.core.*;
import com.robo4j.core.client.util.RoboHttpUtils;
import com.robo4j.core.httpunit.test.HttpCommandTestUnit;
import org.junit.Assert;
import org.junit.Test;

import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.core.httpunit.HttpServerUnit;
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

	//TODO: miro -> continue here
	@Test
	public void simpleHttpNonUnitTest() throws Exception {
		RoboSystem system = new RoboSystem();
		Configuration config = ConfigurationFactory.createEmptyConfiguration();

		HttpServerUnit httpServer = new HttpServerUnit(system, "http");
		config.setString("target", "controller");
		config.setInteger("port", PORT);
		Configuration targetUnits = config.createChildConfiguration(RoboHttpUtils.HTTP_TARGET_UNITS);
		targetUnits.setString("controller", "GET");
		httpServer.initialize(config);

		HttpCommandTestUnit ctrl = new HttpCommandTestUnit(system, "controller");
		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString("target", "request_consumer");
		ctrl.initialize(config);

		StringConsumer consumer = new StringConsumer(system, "request_consumer");

		Assert.assertNotNull(system.getUnits());
		Assert.assertEquals(system.getUnits().size(), 0);
		Assert.assertEquals(httpServer.getState(), LifecycleState.INITIALIZED);
		Assert.assertEquals(system.getState(), LifecycleState.UNINITIALIZED);

		system.addUnits(httpServer, ctrl, consumer);

		System.out.println("State before start:");
		System.out.println(SystemUtil.generateStateReport(system));
		system.start();

		System.out.println("State after start:");
		System.out.println(SystemUtil.generateStateReport(system));

		ctrl.getKnownAttributes().forEach(a -> System.out.println("http://<IP>" + PORT + "/"
				+ a.getAttributeName() + "?<value of:" + a.getAttributeType().getSimpleName() + ">"));

//		System.in.read();

		DefaultAttributeDescriptor<ArrayList> messagesDescriptor = DefaultAttributeDescriptor.create(ArrayList.class, "getReceivedMessages");
		List<String> receivedMessages = consumer.getAttribute(messagesDescriptor).get();
		System.out.println("receivedMessages: " + receivedMessages);


		System.out.println("Going Down!");
		system.stop();
		system.shutdown();
		System.out.println("System is Down!");
		Assert.assertNotNull(system.getUnits());
		Assert.assertEquals(system.getUnits().size(), 3);
//		Assert.assertEquals(consumer.getReceivedMessages().size(), 1);
	}
}
