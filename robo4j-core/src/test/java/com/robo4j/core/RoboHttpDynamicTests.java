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

package com.robo4j.core;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.core.client.util.RoboClassLoader;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.core.unit.HttpServerUnit;
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

		HttpServerUnit httpDynamic = new HttpServerUnit(system, "http_dynamic");
		config.setString("target", "request_consumer");
		config.setInteger("port", PORT);

		/* specific configuration */
		Configuration commands = config.createChildConfiguration("commands");
		commands.setString("path", "tank");
		commands.setString("method", "GET");
		commands.setString("up", "move");
		commands.setString("down", "back");
		commands.setString("left", "right");
		commands.setString("right", "left");
		httpDynamic.initialize(config);

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
		System.out.println("Usage:\n\tRequest GET: http://<IP_ADDRESS>:" + PORT + "/test?command=enter");
		System.out.println("\tRequest command types: right,left,move,back,enter\n");

//		System.in.read();

		System.out.println("Going Down!");
		system.stop();
		system.shutdown();
		System.out.println("System is Down!");
		Assert.assertNotNull(system.getUnits());
		Assert.assertEquals(system.getUnits().size(), 2);
		Assert.assertEquals(consumer.getReceivedMessages().size(), 0);
	}

	@Test
	public void simpleHttpConfiguration() throws ConfigurationException, IOException {
		RoboSystem system = new RoboSystem();
		Configuration config = ConfigurationFactory.createEmptyConfiguration();

		HttpServerUnit httpDynamic = new HttpServerUnit(system, "http");
		config.setInteger("port", PORT);
		config.setString("target", "request_consumer");
		Configuration commands = config.createChildConfiguration("commands");
		commands.setString("path", "tank");
		commands.setString("method", "GET");
		commands.setString("up", "move");
		commands.setString("down", "back");
		commands.setString("left", "right");
		commands.setString("right", "left");
		httpDynamic.initialize(config);

		StringConsumer consumer = new StringConsumer(system, "request_consumer");

		system.addUnits(httpDynamic, consumer);

		System.out.println("State before start:");
		System.out.println(SystemUtil.generateStateReport(system));
		system.start();

		System.out.println("State after start:");
		System.out.println(SystemUtil.generateStateReport(system));

		System.out.println("RoboSystem http server\n\tPort:" + PORT + "\n");
		System.out.println("Usage:\n\tRequest GET: http://<IP_ADDRESS>:" + PORT + "/test?command=enter");
		System.out.println("\tRequest command types: right,left,move,back,enter\n");

//		System.in.read();
		System.out.println("Going Down!");
		system.stop();
		system.shutdown();
		System.out.println("System is Down!");
		Assert.assertNotNull(system.getUnits());
		Assert.assertEquals(system.getUnits().size(), 2);
		Assert.assertEquals(consumer.getReceivedMessages().size(), 0);

	}

	@Test
	public void simpleHttpDeclarative() throws RoboBuilderException, IOException {

		RoboBuilder builder = new RoboBuilder().add(RoboClassLoader.getInstance().getResource("http_get.xml"));
		RoboContext ctx = builder.build();

		Configuration confCommands = ctx.getReference("http").getConfiguration().getChildConfiguration("commands");
		Assert.assertNotNull(confCommands.getValueNames());
		Assert.assertEquals(confCommands.getValueNames().size(), 5);
		Assert.assertEquals(confCommands.getString("up", null), "move");
		Assert.assertEquals(confCommands.getString("down", null), "back");
		Assert.assertEquals(confCommands.getString("right", null), "left");
		Assert.assertEquals(confCommands.getString("left", null), "right");


	}

}
