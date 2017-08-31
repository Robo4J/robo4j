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

package com.robo4j.socket.http.units;

import com.robo4j.socket.http.util.HttpStringProducer;
import com.robo4j.socket.http.util.JsonUtil;
import org.junit.Assert;
import org.junit.Test;

import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboSystem;
import com.robo4j.core.StringConsumer;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.core.util.SystemUtil;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.socket.http.units.test.HttpCommandTestController;

import java.util.Collections;

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
	private static final String REQUEST_CONSUMER = "request_consumer";
	private static final String PACKAGE_CODECS = "com.robo4j.socket.http.units.test.codec";
	//TODO FIXME (miro) -> research why
	private static final int STOPPER = -1;


	//	@Test
	public void pongTest() throws Exception {
		RoboContext systemPong = configurePongSystem();
		systemPong.start();
		System.out.println("systemPong: State after start:");
		System.out.println(SystemUtil.printStateReport(systemPong));
		System.out.println("Press Key...");
		System.in.read();

	}

	@Test
	public void pingPongTest() throws Exception {

		RoboContext systemPong = configurePongSystem();
		RoboContext systemPing = configurePingSystem();

		systemPong.start();
		System.out.println("systemPong: State after start:");
		System.out.println(SystemUtil.printStateReport(systemPong));

		systemPing.start();
		System.out.println("systemPing: State after start:");
		System.out.println(SystemUtil.printStateReport(systemPing));

		System.out.println("systemPing: send messages");
		RoboReference<Object> systemPingProducer = systemPing.getReference("http_producer");
		for (int i = 0; i < MESSAGES; i++) {
			systemPingProducer.sendMessage((HttpStringProducer.SEND_POST_MESSAGE + "::").concat(RoboHttpDynamicTests.JSON_STRING));
		}

		RoboReference<Object> pongConsumer = systemPong.getReference(REQUEST_CONSUMER);

		// FIXME, TODO: 20.08.17 (miro,markus) please implement notification
		Thread.sleep(100);
		System.out.println("systemPing : Going Down!");
		systemPing.stop();
		systemPing.shutdown();


		System.out.println("systemPong : Going Down!");
		final DefaultAttributeDescriptor<Integer> messagesNumberDescriptor = DefaultAttributeDescriptor
				.create(Integer.class, StringConsumer.PROP_GET_NUMBER_OF_SENT_MESSAGES);
		final int number = pongConsumer.getAttribute(messagesNumberDescriptor).get();
		// NOTE: Not working
		systemPong.stop();
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
		config.setString("packages", PACKAGE_CODECS);
		config.setInteger("stopper", STOPPER);
		/* specific configuration */
		config.setString(RoboHttpUtils.HTTP_TARGET_UNITS, JsonUtil.getJsonByMap(Collections.singletonMap(CONTROLLER_PING_PONG, "POST")));
		httpServer.initialize(config);

		StringConsumer consumer = new StringConsumer(result, REQUEST_CONSUMER);
		config = ConfigurationFactory.createEmptyConfiguration();
		consumer.initialize(config);

		HttpCommandTestController ctrl = new HttpCommandTestController(result, CONTROLLER_PING_PONG);
		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString("target", REQUEST_CONSUMER);
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
		config.setString(RoboHttpUtils.HTTP_TARGET_UNITS, JsonUtil.getJsonByMap(Collections.singletonMap(CONTROLLER_PING_PONG, "POST")));
		httpClient.initialize(config);

		HttpStringProducer producer = new HttpStringProducer(result, "http_producer");
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
