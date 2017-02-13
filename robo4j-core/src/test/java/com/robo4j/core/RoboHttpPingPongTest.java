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

package com.robo4j.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

	private static final int PORT = 8025;

	private ExecutorService executor = Executors.newFixedThreadPool(2);

	// @Test
	public void pingPongTest() throws Exception {

		// RoboSystem systemPing = configurePingSystem();
		RoboSystem systemPong = configurePongSystem();

		executor.execute(() -> {
			System.out.println("systemPong: State before start:");
			System.out.println(SystemUtil.generateStateReport(systemPong));
			systemPong.start();
			System.out.println("systemPong: State after start:");
			System.out.println(SystemUtil.generateStateReport(systemPong));

		});

		// executor.execute(() -> {
		// System.out.println("systemPing: State before start:");
		// System.out.println(SystemUtil.generateStateReport(systemPing));
		// systemPing.start();
		// System.out.println("systemPing: State after start:");
		// System.out.println(SystemUtil.generateStateReport(systemPing));
		//
		// try {
		// TimeUnit.SECONDS.sleep(5);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		//
		// System.out.println("systemPing: send messages");
		// systemPing.getReference("http_producer").sendMessage("sendRandomMessage");
		// });

		System.in.read();

		// System.out.println("systemPing : Going Down!");
		// systemPing.stop();
		// systemPing.shutdown();

		System.out.println("systemPong : Going Down!");
		systemPong.stop();
		systemPong.shutdown();

	}

	// Private Methods
	private RoboSystem configurePongSystem() throws Exception {
		final RoboSystem result = new RoboSystem();
		Configuration config = ConfigurationFactory.createEmptyConfiguration();

		HttpServerUnit httpServer = new HttpServerUnit(result, "http_server");
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
		Configuration commands = config.createChildConfiguration("commands");
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
		producer.initialize(config);

		result.addUnits(producer, httpClient);
		return result;
	}
}
