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

import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationFactory;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.units.test.HttpCommandTestController;
import com.robo4j.socket.http.units.test.HttpStringProducer;
import com.robo4j.socket.http.units.test.StringConsumer;
import com.robo4j.socket.http.util.HttpPathConfigJsonBuilder;
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.util.SystemUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PATHS_CONFIG;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_HOST;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_PORT;

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

	private static final String ID_HTTP_PRODUCER = "http_producer";
	private static final String ID_HTTP_CLIENT = "http_client";
	private static final String ID_HTTP_SERVER = "http_server";
	private static final String CONTROLLER_PING_PONG = "controller";
	private static final String HOST_SYSTEM = "0.0.0.0";
	private static final int PORT = 8042;
	private static final int MESSAGES = 3;
	private static final String REQUEST_CONSUMER = "request_consumer";
	private static final String PACKAGE_CODECS = "com.robo4j.socket.http.units.test.codec";
	private static final String HTTP_POST_METHOD = "POST";

	@Ignore
	@Test
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
		RoboReference<Object> systemPingProducer = systemPing.getReference(ID_HTTP_PRODUCER);
		for (int i = 0; i < MESSAGES; i++) {
			systemPingProducer
					.sendMessage(HttpStringProducer.SEND_POST_MESSAGE + "::" + RoboHttpDynamicTests.JSON_STRING);
		}

		RoboReference<Object> pongConsumer = systemPong.getReference(REQUEST_CONSUMER);

		Thread.sleep(500);
		System.out.println("systemPing : Going Down!");
		systemPing.stop();
		systemPing.shutdown();

		System.out.println("systemPong : Going Down!");
		final DefaultAttributeDescriptor<Integer> messagesNumberDescriptor = DefaultAttributeDescriptor
				.create(Integer.class, StringConsumer.PROP_GET_NUMBER_OF_SENT_MESSAGES);
		final int number = pongConsumer.getAttribute(messagesNumberDescriptor).get();
		systemPong.stop();
		Assert.assertEquals(number, MESSAGES);
		System.out.println("PingPong is down!");
		systemPong.shutdown();

	}

	// Private Methods
	private RoboContext configurePongSystem() throws Exception {
		RoboBuilder builder = new RoboBuilder();

		Configuration config = ConfigurationFactory.createEmptyConfiguration();
		config.setInteger(HTTP_PROPERTY_PORT, PORT);
		config.setString("packages", PACKAGE_CODECS);
		/* server path configuration */

		final HttpPathConfigJsonBuilder pathBuilder = HttpPathConfigJsonBuilder.Builder()
				.addPath("controller", HttpMethod.POST);

		config.setString(HTTP_PATHS_CONFIG, pathBuilder.build());

		builder.add(HttpServerUnit.class, config, ID_HTTP_SERVER);
		builder.add(StringConsumer.class, REQUEST_CONSUMER);

		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString("target", REQUEST_CONSUMER);
		builder.add(HttpCommandTestController.class, config, CONTROLLER_PING_PONG);

		return builder.build();
	}

	private RoboContext configurePingSystem() throws Exception {
		RoboBuilder builder = new RoboBuilder();
		List<String> paths = Arrays.asList("units", CONTROLLER_PING_PONG);

		Configuration config = ConfigurationFactory.createEmptyConfiguration();
		config.setString("address", HOST_SYSTEM);
		config.setInteger(HTTP_PROPERTY_PORT, PORT);
		/* specific configuration */
		config.setString(RoboHttpUtils.HTTP_PATHS_CONFIG,
				HttpPathConfigJsonBuilder.Builder().addPath(CONTROLLER_PING_PONG, HttpMethod.POST).build());

		builder.add(HttpClientUnit.class, config, ID_HTTP_CLIENT);

		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString("target", ID_HTTP_CLIENT);
		config.setString("method", "POST");
		config.setString("uri", HttpPathUtils.pathsToUri(paths));
		config.setString(HTTP_PROPERTY_HOST, HOST_SYSTEM);
		config.setInteger(HTTP_PROPERTY_PORT, PORT);
		builder.add(HttpStringProducer.class, config, ID_HTTP_PRODUCER);
		return builder.build();
	}
}
