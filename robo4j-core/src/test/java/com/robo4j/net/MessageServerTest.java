
/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.net;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MessageServerTest {
	private volatile Exception exception = null;

	@Test
	public void testClientServerMessagePassing() throws Exception {
		final List<String> messages = new ArrayList<>();
		final CountDownLatch messageLatch = new CountDownLatch(3);

		Configuration serverConfig = ConfigurationFactory.createEmptyConfiguration();
		serverConfig.setString("ServerName", "Server Name");
		serverConfig.setString(MessageServer.KEY_HOST_NAME, "localhost");
		MessageServer server = new MessageServer( (uuid, id, message) -> {
				System.out.println("Got uuid: " + uuid + " id:" + id + " message:" + message);
				messages.add(String.valueOf(message));
				messageLatch.countDown();
			}, serverConfig);

		Thread t = new Thread(() -> {
				try {
					server.start();
				} catch (IOException e) {
					exception = e;
					Assert.fail(e.getMessage());
				}}, "Server Listener");
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
		MessageClient client = new MessageClient(server.getListeningURI(), "myuuid", clientConfig);
		if (exception != null) {
			throw exception;
		}

		List<String> testMessage = getOrderedTestMessage("My First Little Message!",
				"My Second Little Message!", "My Third Little Message!");
		client.connect();
		for(String message: testMessage){
			client.sendMessage("test", message);
		}
		
		messageLatch.await(2, TimeUnit.SECONDS);
		Assert.assertEquals(testMessage.size(), messages.size());
		Assert.assertArrayEquals(testMessage.toArray(), messages.toArray());
	}

	private List<String> getOrderedTestMessage(String... messages){
		if(messages == null || messages.length == 0){
			Assert.fail("Expected message");
		}
		return Stream.of(messages).collect(Collectors.toCollection(LinkedList::new));
	}

	@Test
	public void testMessageTypes() throws Exception {
		final List<Object> messages = new ArrayList<>();
		final CountDownLatch messageLatch = new CountDownLatch(8);

		Configuration serverConfig = ConfigurationFactory.createEmptyConfiguration();
		serverConfig.setString("ServerName", "Server Name");
		serverConfig.setString(MessageServer.KEY_HOST_NAME, "localhost");
		MessageServer server = new MessageServer( (uuid, id, message) -> {
				System.out.println("Got uuid: " + uuid + " got id:" + id + " message:" + message);
				messages.add(message);
				messageLatch.countDown();
			}, serverConfig);

		Thread t = new Thread(() ->  {
				try {
					server.start();
				} catch (IOException e) {
					exception = e;
					Assert.fail(e.getMessage());
				}}, "Server Listener");
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
		MessageClient client = new MessageClient(server.getListeningURI(), "myuuid", clientConfig);
		if (exception != null) {
			throw exception;
		}
		client.connect();

		client.sendMessage("test1", new Byte((byte) 1));
		client.sendMessage("test2", new Short((short) 2));
		client.sendMessage("test3", new Character((char) 3));
		client.sendMessage("test4", new Integer(4));
		client.sendMessage("test5", new Float(5.0f));
		client.sendMessage("test6", new Long(6));
		client.sendMessage("test7", new Double(7));
		client.sendMessage("test8", new TestMessageType(8, "Lalala", null));
		messageLatch.await(140000000, TimeUnit.SECONDS);

		Assert.assertEquals(8, messages.size());
		if (messages.get(0) instanceof Byte) {
			Assert.assertEquals(((Byte) messages.get(0)).byteValue(), 1);
		} else {
			Assert.fail("Expected Byte!");
		}
		if (messages.get(1) instanceof Short) {
			Assert.assertEquals(((Short) messages.get(1)).shortValue(), 2);
		} else {
			Assert.fail("Expected Short!");
		}
		if (messages.get(2) instanceof Character) {
			Assert.assertEquals(((Character) messages.get(2)).charValue(), 3);
		} else {
			Assert.fail("Expected Character!");
		}
		if (messages.get(3) instanceof Integer) {
			Assert.assertEquals(((Integer) messages.get(3)).intValue(), 4);
		} else {
			Assert.fail("Expected Integer!");
		}
		if (messages.get(4) instanceof Float) {
			Assert.assertEquals(((Float) messages.get(4)).floatValue(), 5.0f, 0.000001);
		} else {
			Assert.fail("Expected Float!");
		}
		if (messages.get(5) instanceof Long) {
			Assert.assertEquals(((Long) messages.get(5)).longValue(), 6);
		} else {
			Assert.fail("Expected Long!");
		}
		if (messages.get(6) instanceof Double) {
			Assert.assertEquals(((Double) messages.get(6)).doubleValue(), 7.0, 0.000001);
		} else {
			Assert.fail("Expected Double!");
		}
		if (messages.get(7) instanceof TestMessageType) {
			TestMessageType message = (TestMessageType) messages.get(7);
			Assert.assertEquals(message.getNumber(), 8);
			Assert.assertEquals(message.getText(), "Lalala");
		} else {
			Assert.fail("Expected TestMessageType!");
		}
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
