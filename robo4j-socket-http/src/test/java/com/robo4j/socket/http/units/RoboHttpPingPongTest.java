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

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.units.test.HttpCommandTestController;
import com.robo4j.socket.http.units.test.SocketMessageDecoratedProducerUnit;
import com.robo4j.socket.http.units.test.StringConsumer;
import com.robo4j.socket.http.util.HttpPathConfigJsonBuilder;
import com.robo4j.util.SystemUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_HOST;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_SOCKET_PORT;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_TARGET;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_UNIT_PATHS_CONFIG;

/**
 * Ping Pong test from outside/foreign unit is send signal. The signal has been
 * received by HttpServer unit. HttpServer unit propagates the signal to the
 * target unit.
 * <p>
 * (FU)<- client gets response from the server ->(SU)->(TU)
 * <p>
 * Test communicates over socket on PORT
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboHttpPingPongTest {

	public static final String PACKAGE_CODECS = "com.robo4j.socket.http.units.test.codec";
	public static final String HOST_SYSTEM = "localhost";
	private static final int TIMEOUT = 10;
	private static final TimeUnit TIME_UNIT = TimeUnit.HOURS;
	private static final String ID_HTTP_CLIENT = "http_client";
	private static final String ID_HTTP_SERVER = "http_server";
	private static final String CONTROLLER_PING_PONG = "controller";
	private static final int PORT = 8042;
	private static final int MESSAGES = 50;
	private static final String REQUEST_CONSUMER = "request_consumer";
	private static final String DECORATED_PRODUCER = "decoratedProducer";

	@Ignore
	@Test
	public void pongTest() throws Exception {
		RoboContext system = configurePongSystem(0);
		system.start();
		System.out.println("systemPong: State after start:");
		System.out.println(SystemUtil.printStateReport(system));
		System.out.println("Press Key...");
		System.in.read();
		system.shutdown();

	}

	@Test
	public void pingPongTest() throws Exception {

		RoboContext systemPong = configurePongSystem(MESSAGES);
		RoboContext systemPing = configurePingSystem();

		systemPong.start();
		System.out.println("systemPong: State after start:");
		System.out.println(SystemUtil.printStateReport(systemPong));

		systemPing.start();
		System.out.println("systemPing: State after start:");
		System.out.println(SystemUtil.printStateReport(systemPing));

		System.out.println("systemPing: send messages");
		RoboReference<Object> decoratedProducer = systemPing.getReference(DECORATED_PRODUCER);
		decoratedProducer.sendMessage(MESSAGES);

		RoboReference<String> pongConsumer = systemPong.getReference(REQUEST_CONSUMER);
		CountDownLatch attributeFuture = pongConsumer.getAttribute(StringConsumer.DESCRIPTOR_COUNT_DOWN_LATCH).get();

		attributeFuture.await(TIMEOUT, TIME_UNIT);
		System.out.println("systemPing : Going Down!");
		systemPing.stop();
		systemPing.shutdown();

		System.out.println("systemPong : Going Down!");

		final int number = pongConsumer.getAttribute(StringConsumer.DESCRIPTOR_MESSAGES_NUMBER_TOTAL).get();
		systemPong.stop();
		Assert.assertEquals(number, MESSAGES);
		System.out.println("PingPong is down!");
		systemPong.shutdown();

	}

	// Private Methods
	private RoboContext configurePongSystem(int totalMessageNumber) throws Exception {
		RoboBuilder builder = new RoboBuilder();

		final HttpPathConfigJsonBuilder pathBuilder = HttpPathConfigJsonBuilder.Builder().addPath(CONTROLLER_PING_PONG, HttpMethod.POST);

		Configuration config = new ConfigurationBuilder().addInteger(PROPERTY_SOCKET_PORT, PORT).addString("packages", PACKAGE_CODECS)
				.addString(PROPERTY_UNIT_PATHS_CONFIG, pathBuilder.build()).build();

		builder.add(HttpServerUnit.class, config, ID_HTTP_SERVER);

		config = new ConfigurationBuilder().addInteger(StringConsumer.PROP_TOTAL_NUMBER_MESSAGES, totalMessageNumber).build();
		builder.add(StringConsumer.class, config, REQUEST_CONSUMER);

		config = new ConfigurationBuilder().addString("target", REQUEST_CONSUMER).build();
		builder.add(HttpCommandTestController.class, config, CONTROLLER_PING_PONG);

		return builder.build();
	}

	private RoboContext configurePingSystem() throws Exception {

		/* system which is testing main system */
		RoboBuilder builder = new RoboBuilder();

		Configuration config = new ConfigurationBuilder().addString(PROPERTY_HOST, HOST_SYSTEM).addInteger(PROPERTY_SOCKET_PORT, PORT)
				.build();
		builder.add(HttpClientUnit.class, config, ID_HTTP_CLIENT);

		config = new ConfigurationBuilder().addString(PROPERTY_TARGET, ID_HTTP_CLIENT)
				.addString(PROPERTY_UNIT_PATHS_CONFIG, "[{\"roboUnit\":\"" + CONTROLLER_PING_PONG + "\",\"method\":\"POST\"}]")
				.addString("message", RoboHttpDynamicTests.JSON_STRING).build();
		builder.add(SocketMessageDecoratedProducerUnit.class, config, DECORATED_PRODUCER);

		return builder.build();
	}
}
