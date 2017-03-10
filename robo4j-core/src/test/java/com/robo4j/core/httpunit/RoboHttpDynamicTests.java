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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;

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

	private ExecutorService executor = Executors.newFixedThreadPool(1);

	/**
	 * Motivation Client system is sending messages to the main system over HTTP
	 * Main System receives desired number of messages.
	 *
	 * Values are requested by Attributes
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
//	@Test
	public void simpleHttpNonUnitTest() throws Exception {

		/* system which is testing main system */
		RoboSystem clientSystem = getClientRoboSystem();

		HttpClientUnit httpClient = new HttpClientUnit(clientSystem, "httpClient");

		System.out.println("Client State after start:");
		System.out.println(SystemUtil.generateStateReport(clientSystem));
		clientSystem.start();

		/* tested system configuration */
		RoboSystem mainSystem = getServerRoboSystem();


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


		System.out.println("Going Down!");
		mainSystem.stop();
		mainSystem.shutdown();
		System.out.println("System is Down!");
		Assert.assertNotNull(mainSystem.getUnits());
		Assert.assertEquals(mainSystem.getUnits().size(), MESSAGES_NUMBER);

	}

	private RoboSystem getServerRoboSystem() throws Exception {
		/* tested system configuration */
		RoboSystem result = new RoboSystem();

		Configuration config = ConfigurationFactory.createEmptyConfiguration();
		HttpServerUnit httpServer = new HttpServerUnit(result, "http");
		config.setString("target", TARGET_UNIT);
		config.setInteger("port", PORT);
		Configuration targetUnits = config.createChildConfiguration(RoboHttpUtils.HTTP_TARGET_UNITS);
		targetUnits.setString(TARGET_UNIT, "GET");
		httpServer.initialize(config);

		HttpCommandTestController ctrl = new HttpCommandTestController(result, TARGET_UNIT);
		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString("target", "request_consumer");
		ctrl.initialize(config);

		StringConsumer consumer = new StringConsumer(result, "request_consumer");

		Assert.assertNotNull(result.getUnits());
		Assert.assertEquals(result.getUnits().size(), 0);
		Assert.assertEquals(httpServer.getState(), LifecycleState.INITIALIZED);
		Assert.assertEquals(result.getState(), LifecycleState.UNINITIALIZED);

		result.addUnits(httpServer, ctrl, consumer);

		SystemUtil.generateSocketPoint(httpServer, ctrl);
		return result;

	}

	private RoboSystem getClientRoboSystem() throws Exception {
		/* system which is testing main system */
		Configuration config = ConfigurationFactory.createEmptyConfiguration();
		RoboSystem result = new RoboSystem();

		HttpClientUnit httpClient = new HttpClientUnit(result, "httpClient");
		config.setString("address", HOST_SYSTEM);
		config.setInteger("port", PORT);
		/* specific configuration */
		Configuration configuration = config.createChildConfiguration(RoboHttpUtils.HTTP_TARGET_UNITS);
		configuration.setString("controller", "GET");
		httpClient.initialize(config);
		result.addUnits(httpClient);
		System.out.println("Client State after start:");
		System.out.println(SystemUtil.generateStateReport(result));
		return result;
	}
}
