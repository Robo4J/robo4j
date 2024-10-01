/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.test.units;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.configuration.ConfigurationFactory;
import com.robo4j.socket.http.message.DatagramDecoratedRequest;
import com.robo4j.socket.http.message.DatagramDenominator;
import com.robo4j.socket.http.test.units.config.StringConsumer;
import com.robo4j.socket.http.units.DatagramClientUnit;
import com.robo4j.socket.http.units.DatagramServerUnit;
import com.robo4j.socket.http.util.DatagramBodyType;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.util.SystemUtil;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.robo4j.socket.http.test.units.HttpUnitTests.CODECS_UNITS_TEST_PACKAGE;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_PACKAGES;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_HOST;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_SOCKET_PORT;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_UNIT_PATHS_CONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class RoboDatagramPingPongTest {

	private static final int TIMEOUT = 20;
	private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
	private static final String UDP_CLIENT = "udp_client";
	private static final String UDP_SERVER = "udp_server";

	private static final int TOTAL_NUMBER = 122;

	@Test
	void datagramPingPongTest() throws Exception {
		RoboContext pongSystem = configurePongSystem(TOTAL_NUMBER);
		RoboContext pingSystem = configurePingSystem();

		pongSystem.start();
		pingSystem.start();
		System.out.println("UDP pongSystem: State after start:");
		System.out.println(SystemUtil.printStateReport(pongSystem));
		System.out.println("UDP pingSystem: State after start:");
		System.out.println(SystemUtil.printStateReport(pingSystem));

		RoboReference<String> pongStringConsumerReference = pongSystem.getReference(StringConsumer.NAME);
		CountDownLatch totalMessageLatch = pongStringConsumerReference.getAttribute(StringConsumer.DESCRIPTOR_MESSAGES_LATCH).get();

		RoboReference<DatagramDecoratedRequest> udpClient = pingSystem.getReference(UDP_CLIENT);
		for (int i = 0; i < TOTAL_NUMBER; i++) {
			DatagramDenominator denominator = new DatagramDenominator(DatagramBodyType.JSON.getType(), "/units/stringConsumer");
			DatagramDecoratedRequest request = new DatagramDecoratedRequest(denominator);
			String message = "{\"message\": \"Hello i:" + i + "\"}";
			request.addMessage(message.getBytes());
			udpClient.sendMessage(request);
		}

		totalMessageLatch.await(TIMEOUT, TIME_UNIT);
		final int pongConsumerTotalNumber = pongStringConsumerReference.getAttribute(StringConsumer.DESCRIPTOR_MESSAGES_TOTAL).get();


		System.out.println("UDP pongSystem: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(pongSystem));
		System.out.println("UDP pingSystem: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(pingSystem));
		pingSystem.shutdown();
		pongSystem.shutdown();

		assertTrue(pongConsumerTotalNumber > 0 && pongConsumerTotalNumber <= TOTAL_NUMBER );

	}

	private RoboContext configurePingSystem() throws Exception {
		RoboBuilder builder = new RoboBuilder();

		Configuration config = new ConfigurationBuilder().addString(PROPERTY_CODEC_PACKAGES, CODECS_UNITS_TEST_PACKAGE)
				.addString(PROPERTY_HOST, "localhost").addInteger(PROPERTY_SOCKET_PORT, RoboHttpUtils.DEFAULT_UDP_PORT)
				.addString(PROPERTY_UNIT_PATHS_CONFIG, "[{\"roboUnit\":\"stringConsumer\",\"callbacks\": [\"stringConsumer\"]}]").build();
		builder.add(DatagramClientUnit.class, config, UDP_CLIENT);

		config = ConfigurationFactory.createEmptyConfiguration();
		builder.add(StringConsumer.class, config, StringConsumer.NAME);

		return builder.build();
	}

	/**
	 * create simple UDP server with consumer unit
	 *
	 *
	 * @return roboContext
	 * @throws Exception
	 *             exception
	 */
	private RoboContext configurePongSystem(int totalNumberOfMessage) throws Exception {
		RoboBuilder builder = new RoboBuilder();
		Configuration config = new ConfigurationBuilder().addString(PROPERTY_CODEC_PACKAGES, CODECS_UNITS_TEST_PACKAGE)
				.addString(PROPERTY_UNIT_PATHS_CONFIG, "[{\"roboUnit\":\"stringConsumer\",\"filters\":[]}]").build();
		builder.add(DatagramServerUnit.class, config, UDP_SERVER);

		config = new ConfigurationBuilder().addInteger(StringConsumer.PROP_TOTAL_NUMBER_MESSAGES, totalNumberOfMessage).build();
		builder.add(StringConsumer.class, config, StringConsumer.NAME);

		return builder.build();
	}
}
