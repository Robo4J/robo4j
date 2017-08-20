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

package com.robo4j.core.units.httpunit;

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboSystem;
import com.robo4j.core.StringConsumer;
import com.robo4j.core.StringProducer;
import com.robo4j.core.client.util.RoboHttpUtils;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.core.units.httpunit.test.HttpCommandTestController;
import com.robo4j.core.util.SystemUtil;

/**
 * Ping Pong test from outside/foreign unit is send signal. The signal has been
 * received by HttpServer unit. HttpServer unit propagates the signal to the
 * target unit.
 *
 * (FU)<- client gets response from the server ->(SU)->(TU)
 *
 * Test communicates over socket on PORT
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboHttpPingPongTest {

	private static final String CONTROLLER_PING_PONG = "controller";
	private static final String HOST_SYSTEM = "0.0.0.0";
	private static final int PORT = 8042;
	private static final int MESSAGES = 3;

	// FIXME: 20.08.17 miro -> review
	@Test
	public void pingPongTest() throws Exception {

		RoboContext systemPong = configurePongSystem();
		RoboContext systemPing = configurePingSystem();


		System.out.println("systemPong: State before start:");
		System.out.println(SystemUtil.printStateReport(systemPong));
		systemPong.start();

		System.out.println("systemPing: State before start:");
		System.out.println(SystemUtil.printStateReport(systemPing));
		systemPing.start();

		System.out.println("systemPong: State after start:");
		System.out.println(SystemUtil.printStateReport(systemPong));


		System.out.println("systemPing: State after start:");
		System.out.println(SystemUtil.printStateReport(systemPing));
		System.out.println("systemPing: send messages");
		RoboReference<Object> systemPingProducer = systemPing.getReference("http_producer");
		for (int i = 0; i < MESSAGES; i++) {
			systemPingProducer.sendMessage("sendPostMessage::".concat(RoboHttpDynamicTests.JSON_STRING));
		}

		RoboReference<Object> pongConsumer = systemPong.getReference("request_consumer");

		// FIXME, TODO: 20.08.17 (miro,markus) please implement notification
		Thread.sleep(1000);
		System.out.println("systemPing : Going Down!");
		systemPing.stop();
		systemPing.shutdown();

		System.out.println("systemPong : Going Down!");
		systemPong.stop();

		final DefaultAttributeDescriptor<Integer> messagesNumberDescriptor = DefaultAttributeDescriptor
				.create(Integer.class, "getNumberOfSentMessages");
		final int number = pongConsumer.getAttribute(messagesNumberDescriptor).get();
		// NOTE: Not working
		Assert.assertEquals(number, MESSAGES);
		System.out.println("PingPong is down!");
		systemPong.shutdown();

	}

	// Private Methods
	private RoboContext configurePongSystem() throws Exception {
		final RoboSystem result = new RoboSystem();
		Configuration config = ConfigurationFactory.createEmptyConfiguration();

		HttpServerUnit httpServer = new HttpServerUnit(result, "http_server");
		config.setString("target", CONTROLLER_PING_PONG);
		config.setInteger("port", PORT);
		config.setString("packages", "com.robo4j.core.units.httpunit.test.codec");
		/* specific configuration */
		Configuration targetUnits = config.createChildConfiguration(RoboHttpUtils.HTTP_TARGET_UNITS);
		targetUnits.setString(CONTROLLER_PING_PONG, "POST");
		httpServer.initialize(config);

		StringConsumer consumer = new StringConsumer(result, "request_consumer");
		config = ConfigurationFactory.createEmptyConfiguration();
		consumer.initialize(config);

		HttpCommandTestController ctrl = new HttpCommandTestController(result, CONTROLLER_PING_PONG);
		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString("target", "request_consumer");
		ctrl.initialize(config);

		result.addUnits(httpServer, consumer, ctrl);
		return result;
	}

	private RoboContext configurePingSystem() throws Exception {
		final RoboSystem result = new RoboSystem();
		Configuration config = ConfigurationFactory.createEmptyConfiguration();

		HttpClientUnit httpClient = new HttpClientUnit(result, "http_client");
		config.setString("address", HOST_SYSTEM);
		config.setInteger("port", PORT);
		/* specific configuration */
		Configuration targetUnits = config.createChildConfiguration(RoboHttpUtils.HTTP_TARGET_UNITS);
		targetUnits.setString(CONTROLLER_PING_PONG, "POST");

		httpClient.initialize(config);

		StringProducer producer = new StringProducer(result, "http_producer");
		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString("target", "http_client");
		config.setString("method", "POST");
		config.setString("uri", "/controller");
		config.setString("targetAddress", HOST_SYSTEM);
		producer.initialize(config);

		result.addUnits(producer, httpClient);
		return result;
	}
}
