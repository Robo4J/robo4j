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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.socket.http.units;

import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.LifecycleState;
import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationFactory;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.units.test.HttpCommandTestController;
import com.robo4j.socket.http.units.test.HttpMessageDecoratedProducerUnit;
import com.robo4j.socket.http.units.test.StringConsumer;
import com.robo4j.socket.http.util.HttpPathConfigJsonBuilder;
import com.robo4j.util.SystemUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Future;

import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_UNIT_PATHS_CONFIG;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_HOST;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_PORT;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_TARGET;

/**
 *
 * Dynamic HttpUnit request/method configuration
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboHttpDynamicTests {

	private static final String ID_HTTP_SERVER = "http";
	private static final int PORT = 8025;
	private static final int SLEEP_DELAY = 400; // necessary delay due to multi-threading we should fix it
	private static final String ID_CLIENT_UNIT = "httpClient";
	private static final String ID_TARGET_UNIT = "controller";
	private static final int MESSAGES_NUMBER = 42;
	private static final String HOST_SYSTEM = "0.0.0.0";
	private static final String REQUEST_CONSUMER = "request_consumer";
	static final String JSON_STRING = "{\"value\":\"stop\"}";
	private static final String DECORATED_PRODUCER = "decoratedProducer";

	/**
	 * Motivation Client system is sending messages to the main system over HTTP
	 * Main System receives desired number of messages.
	 *
	 * Values are requested by Attributes
	 * 
	 * @throws Exception
	 */
	@Test
	public void simpleHttpNonUnitTest() throws Exception {

		/* tested system configuration */
		RoboContext mainSystem = getServerRoboSystem();

		/* system which is testing main system */
		RoboContext clientSystem = getClientRoboSystem();


		System.out.println("Client system state after start:");
		System.out.println(SystemUtil.printStateReport(clientSystem));

		System.out.println("Main system state after start:");
		System.out.println(SystemUtil.printStateReport(mainSystem));

		/* client system sending a messages to the main system */
        RoboReference<Object> decoratedProducer = clientSystem.getReference(DECORATED_PRODUCER);
		decoratedProducer.sendMessage(MESSAGES_NUMBER);

		Thread.sleep(SLEEP_DELAY);

		clientSystem.stop();
		clientSystem.shutdown();

		System.out.println("Going Down!");

		final DefaultAttributeDescriptor<Integer> descriptor = DefaultAttributeDescriptor.create(Integer.class,
				StringConsumer.PROP_GET_NUMBER_OF_SENT_MESSAGES);
		final Future<Integer> messagesFuture = mainSystem.getReference(REQUEST_CONSUMER).getAttribute(descriptor);
		final int receivedMessages = messagesFuture.get();

		Thread.sleep(SLEEP_DELAY);
		mainSystem.stop();
		mainSystem.shutdown();

		System.out.println("System is Down!");
		Assert.assertNotNull(mainSystem.getUnits());
		Assert.assertEquals("wrong received messages", receivedMessages, MESSAGES_NUMBER);
	}

	// Private Methods
	private RoboContext getServerRoboSystem() throws Exception {
		/* tested system configuration */
		RoboBuilder builder = new RoboBuilder();

		Configuration config = ConfigurationFactory.createEmptyConfiguration();
		config.setInteger(HTTP_PROPERTY_PORT, PORT);
		config.setString("packages", "com.robo4j.socket.http.units.test.codec");
		config.setString(HTTP_UNIT_PATHS_CONFIG,
				HttpPathConfigJsonBuilder.Builder().addPath(ID_TARGET_UNIT, HttpMethod.POST).build());
		builder.add(HttpServerUnit.class, config, ID_HTTP_SERVER);


		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString(HTTP_PROPERTY_TARGET, REQUEST_CONSUMER);
		builder.add(HttpCommandTestController.class, config, ID_TARGET_UNIT);

		builder.add(StringConsumer.class, REQUEST_CONSUMER);

		RoboContext result = builder.build();
		Assert.assertNotNull(result.getUnits());
		Assert.assertEquals(result.getUnits().size(), 3);
		Assert.assertEquals(result.getReference(ID_HTTP_SERVER).getState(), LifecycleState.INITIALIZED);
		Assert.assertEquals(result.getState(), LifecycleState.INITIALIZED);

		result.start();
		System.out.println(SystemUtil.printSocketEndPoint(result.getReference(ID_HTTP_SERVER),
				result.getReference(ID_TARGET_UNIT)));
		return result;
	}

	private RoboContext getClientRoboSystem() throws Exception {
		/* system which is testing main system */
		RoboBuilder builder = new RoboBuilder();

        Configuration config = ConfigurationFactory.createEmptyConfiguration();
        config.setString(HTTP_PROPERTY_HOST, HOST_SYSTEM);
        config.setInteger(HTTP_PROPERTY_PORT, PORT);
		builder.add(HttpClientUnit.class, config, ID_CLIENT_UNIT);

		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString(HTTP_PROPERTY_TARGET, ID_CLIENT_UNIT);
		config.setString(HTTP_UNIT_PATHS_CONFIG, "[{\"roboUnit\":\""+ ID_TARGET_UNIT + "\",\"method\":\"POST\"}]");
		config.setString("message", JSON_STRING);
		builder.add(HttpMessageDecoratedProducerUnit.class, config, DECORATED_PRODUCER);

		RoboContext result = builder.build();
		result.start();
		return result;
	}
}
