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
package com.robo4j.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationFactory;

public class MessageServerTest {
	private volatile Exception exception = null;

	@Test
	public void testClientServerMessagePassing() throws Exception {
		final List<String> messages = new ArrayList<>();
		final CountDownLatch messageLatch = new CountDownLatch(2);

		Configuration serverConfig = ConfigurationFactory.createEmptyConfiguration();
		serverConfig.setString("ServerName", "Server Name");
		serverConfig.setString(MessageServer.KEY_HOST_NAME, "localhost");
		MessageServer server = new MessageServer(new MessageCallback() {
			@Override
			public void handleMessage(String id, Object message) {
				System.out.println("Got id:" + id + " message:" + message);
				messages.add(String.valueOf(message));
				messageLatch.countDown();
			}
		}, serverConfig);

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					server.start();
				} catch (IOException e) {
					exception = e;
					Assert.fail(e.getMessage());
				}
			}
		}, "Server Listener");
		t.setDaemon(true);
		t.start();
		for (int i = 0; i < 10; i++) {
			if (server.getListeningPort() == 0) {
				Thread.sleep(250);
			} else {
				break;
			}
		}

		Configuration clientConfig = ConfigurationFactory.createEmptyConfiguration();
		MessageClient client = new MessageClient(server.getListeningURI(), clientConfig);
		if (exception != null) {
			throw exception;
		}
		client.connect();
		client.sendMessage("test", "My First Little Message!");
		client.sendMessage("test", "My Second Little Message!");
		messageLatch.await(14, TimeUnit.SECONDS);
		Assert.assertEquals(2, messages.size());
	}

	public static RoboContext createTestContext() {
		RoboTestContext testContext = new RoboTestContext("TestContext", ConfigurationFactory.createEmptyConfiguration());
		Configuration configuration = ConfigurationFactory.createEmptyConfiguration();
		configuration.setString("name", "Test");
		configuration.setString("description", "Lalalala");
		testContext.addRef(new RoboTestReference("test", configuration));
		return testContext;
	}
}
