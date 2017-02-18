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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.unit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboSystem;
import com.robo4j.core.StringConsumer;
import com.robo4j.core.StringProducer;
import com.robo4j.core.client.util.RoboHttpUtils;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.core.unit.HttpClientUnit;
import com.robo4j.core.unit.HttpServerUnit;
import com.robo4j.core.util.SystemUtil;

/**
 * Ping Pong test from outside/foreign unit is send signal. The signal has been
 * received by HttpServer unit. HttpServer unit propagates the signal to the
 * target unit.
 *
 * (FU)<- client gets response from the server ->(SU)->(TU)
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboHttpPingPongTest {

	private static final int PORT = 8011;
    private static final String TEST_PATH ="tank";
	private static final int MESSAGES = 3;


	private ExecutorService executor = Executors.newFixedThreadPool(2);

	@Test
	public void pingPongTest() throws Exception {

		RoboSystem systemPong = configurePongSystem();
        RoboSystem systemPing = configurePingSystem();

		executor.execute(() -> {
			System.out.println("systemPong: State before start:");
			System.out.println(SystemUtil.generateStateReport(systemPong));
			systemPong.start();
			System.out.println("systemPong: State after start:");
			System.out.println(SystemUtil.generateStateReport(systemPong));
		});

		executor.execute(() -> {
			System.out.println("systemPing: State before start:");
			System.out.println(SystemUtil.generateStateReport(systemPing));
			systemPing.start();
			System.out.println("systemPing: State after start:");
			System.out.println(SystemUtil.generateStateReport(systemPing));
			System.out.println("systemPing: send messages");
			RoboReference<Object> systemPingProducer = systemPing.getReference("http_producer");
			for (int i = 0; i < MESSAGES; i++) {
				systemPingProducer.sendMessage("sendGetMessage::".concat(TEST_PATH).concat("?").concat("command=move"));
			}
		});



        StringConsumer pongConsumer = (StringConsumer) systemPong.getUnits().stream()
                .filter(e -> e.getId().equals("request_consumer"))
                .findFirst().get();

		System.out.println("systemPing : Going Down!");
        systemPing.stop();
        systemPing.shutdown();

		System.out.println("systemPong : Going Down!");
		systemPong.stop();
        systemPong.shutdown();
        System.out.println("PingPong is down!");
		Assert.assertEquals(pongConsumer.getReceivedMessages().size(), MESSAGES);

	}

	// Private Methods
	private RoboSystem configurePongSystem() throws Exception {
		final RoboSystem result = new RoboSystem();
		Configuration config = ConfigurationFactory.createEmptyConfiguration();

		HttpServerUnit httpServer = new HttpServerUnit(result, "http_server");
		config.setString("target", "request_consumer");
		config.setInteger("port", PORT);

		/* specific configuration */
		Configuration commands = config.createChildConfiguration(RoboHttpUtils.HTTP_COMMANDS);
		commands.setString("path", TEST_PATH);
		commands.setString("method", "GET");
		commands.setString("up", "move");
		commands.setString("down", "back");
		commands.setString("left", "right");
		commands.setString("right", "left");
		httpServer.initialize(config);

		StringConsumer consumer = new StringConsumer(result, "request_consumer");
		config = ConfigurationFactory.createEmptyConfiguration();
		consumer.initialize(config);
		result.addUnits(httpServer, consumer);
		return result;
	}

	private RoboSystem configurePingSystem() throws Exception {
		final RoboSystem result = new RoboSystem();
		Configuration config = ConfigurationFactory.createEmptyConfiguration();

		HttpClientUnit httpClient = new HttpClientUnit(result, "http_client");
		config.setString("address", "localhost");
		config.setInteger("port", PORT);
		/* specific configuration */
		Configuration commands = config.createChildConfiguration(RoboHttpUtils.HTTP_COMMANDS);
		commands.setString("path", "tank");
		commands.setString("method", "GET");
		commands.setString("up", "move");
		commands.setString("down", "back");
		commands.setString("left", "right");
		commands.setString("right", "left");

		httpClient.initialize(config);

		StringProducer producer = new StringProducer(result, "http_producer");
		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString("target", "http_client");
		config.setString("method", "GET");
		producer.initialize(config);

		result.addUnits(producer, httpClient);
		return result;
	}
}
