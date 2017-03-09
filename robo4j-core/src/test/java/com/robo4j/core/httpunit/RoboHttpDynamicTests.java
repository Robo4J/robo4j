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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboSystem;
import com.robo4j.core.StringConsumer;
import com.robo4j.core.client.util.RoboHttpUtils;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.core.httpunit.test.HttpCommandTestController;
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
	private static final String TARGET_UNIT = "controller";
	private static final int MESSAGES_NUMBER = 3;
	private static final String HOST_SYSTEM = "0.0.0.0";

	/**
	 * Motivation Client system is sending messages to the main system over HTTP
	 * Main System receives desired number of messages.
	 *
	 * Values are requested by Attributes
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void simpleHttpNonUnitTest() throws Exception {

		/* system which is testing main system */
		Configuration config = ConfigurationFactory.createEmptyConfiguration();
		RoboSystem clientSystem = new RoboSystem();

		HttpClientUnit httpClient = new HttpClientUnit(clientSystem, "httpClient");
		config.setString("address", HOST_SYSTEM);
		config.setInteger("port", PORT);
		/* specific configuration */
		Configuration targetUnits = config.createChildConfiguration(RoboHttpUtils.HTTP_TARGET_UNITS);
		targetUnits.setString("controller", "GET");
		httpClient.initialize(config);
		clientSystem.addUnits(httpClient);
		System.out.println("Client State after start:");
		System.out.println(SystemUtil.generateStateReport(clientSystem));
		clientSystem.start();

		/* tested system configuration */
		RoboSystem mainSystem = new RoboSystem();

		HttpServerUnit httpServer = new HttpServerUnit(mainSystem, "http");
		config.setString("target", TARGET_UNIT);
		config.setInteger("port", PORT);
		targetUnits = config.createChildConfiguration(RoboHttpUtils.HTTP_TARGET_UNITS);
		targetUnits.setString(TARGET_UNIT, "GET");
		httpServer.initialize(config);

		HttpCommandTestController ctrl = new HttpCommandTestController(mainSystem, TARGET_UNIT);
		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString("target", "request_consumer");
		ctrl.initialize(config);

		StringConsumer consumer = new StringConsumer(mainSystem, "request_consumer");

		Assert.assertNotNull(mainSystem.getUnits());
		Assert.assertEquals(mainSystem.getUnits().size(), 0);
		Assert.assertEquals(httpServer.getState(), LifecycleState.INITIALIZED);
		Assert.assertEquals(mainSystem.getState(), LifecycleState.UNINITIALIZED);

		mainSystem.addUnits(httpServer, ctrl, consumer);

		System.out.println("State before start:");
		System.out.println(SystemUtil.generateStateReport(mainSystem));
		mainSystem.start();

		System.out.println("State after start:");
		System.out.println(SystemUtil.generateStateReport(mainSystem));

		/* client system sending a messages to the main system */
		for (int i = 0; i < MESSAGES_NUMBER; i++) {
			httpClient.onMessage(RoboHttpUtils.createGetRequest(HOST_SYSTEM,
					"/".concat(TARGET_UNIT).concat("?").concat("command=move")));
		}
		clientSystem.stop();
		clientSystem.shutdown();

		SystemUtil.generateSocketPoint(httpServer, ctrl);


		/* used only for standalone test */
		// System.in.read();
		DefaultAttributeDescriptor<ArrayList> messagesDescriptor = DefaultAttributeDescriptor.create(ArrayList.class,
				"getReceivedMessages");
		DefaultAttributeDescriptor<Integer> messagesNumberDescriptor = DefaultAttributeDescriptor.create(Integer.class,
				"getNumberOfSentMessages");
		final List<String> receivedMessages = consumer.getAttribute(messagesDescriptor).get();
		System.out.println("receivedMessages: " + receivedMessages);
		Assert.assertEquals(receivedMessages.size(), MESSAGES_NUMBER);
		final int number = consumer.getAttribute(messagesNumberDescriptor).get();
		Assert.assertEquals(number, MESSAGES_NUMBER);

		System.out.println("Going Down!");
		mainSystem.stop();
		mainSystem.shutdown();
		System.out.println("System is Down!");
		Assert.assertNotNull(mainSystem.getUnits());
		Assert.assertEquals(mainSystem.getUnits().size(), MESSAGES_NUMBER);

	}
}
