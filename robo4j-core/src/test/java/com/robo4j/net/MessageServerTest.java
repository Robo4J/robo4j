/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.configuration.ConfigurationFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class MessageServerTest {
	private static final String CONST_MYUUID = "myuuid";
	private static final String PROPERTY_SERVER_NAME = "ServerName";
	private volatile Exception exception = null;

	@Test
	void testClientServerMessagePassing() throws Exception {
		final List<String> messages = new ArrayList<>();
		final CountDownLatch messageLatch = new CountDownLatch(3);

		Configuration serverConfig = new ConfigurationBuilder().addString(PROPERTY_SERVER_NAME, "Server Name")
				.addString(MessageServer.KEY_HOST_NAME, "localhost").build();
		MessageServer server = new MessageServer((uuid, id, message) -> {
			System.out.println("Got uuid: " + uuid + " id:" + id + " message:" + message);
			messages.add(String.valueOf(message));
			messageLatch.countDown();
		}, serverConfig);

		Thread t = new Thread(() -> {
			try {
				server.start();
			} catch (IOException e) {
				exception = e;
				fail(e.getMessage());
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
		MessageClient client = new MessageClient(server.getListeningURI(), CONST_MYUUID, clientConfig);
		if (exception != null) {
			throw exception;
		}

		List<String> testMessage = getOrderedTestMessage("My First Little Message!", "My Second Little Message!",
				"My Third Little Message!");
		client.connect();
		for (String message : testMessage) {
			client.sendMessage("test", message);
		}

		messageLatch.await(2, TimeUnit.SECONDS);
		assertEquals(testMessage.size(), messages.size());
		assertArrayEquals(testMessage.toArray(), messages.toArray());
	}

	private List<String> getOrderedTestMessage(String... messages) {
		if (messages == null || messages.length == 0) {
			fail("Expected message");
		}
		return Stream.of(messages).collect(Collectors.toCollection(LinkedList::new));
	}

	@Test
	void testMessageTypes() throws Exception {
		final String messageText = "Lalala";
		final int messagesNumber = 8;
		final List<Object> messages = new ArrayList<>(messagesNumber);
		final CountDownLatch messageLatch = new CountDownLatch(messagesNumber);

		Configuration serverConfig = new ConfigurationBuilder().addString(PROPERTY_SERVER_NAME, "Server Name")
				.addString(MessageServer.KEY_HOST_NAME, "localhost").build();
		MessageServer server = new MessageServer((uuid, id, message) -> {
			System.out.println("Got uuid: " + uuid + " got id:" + id + " message:" + message);
			messages.add(message);
			messageLatch.countDown();
		}, serverConfig);

		Thread t = new Thread(() -> {
			try {
				server.start();
			} catch (IOException e) {
				exception = e;
				fail(e.getMessage());
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
		MessageClient client = new MessageClient(server.getListeningURI(), CONST_MYUUID, clientConfig);
		if (exception != null) {
			throw exception;
		}
		client.connect();

		client.sendMessage("test1", Byte.valueOf((byte) 1));
		client.sendMessage("test2", Short.valueOf((short) 2));
		client.sendMessage("test3", Character.valueOf((char) 3));
		client.sendMessage("test4", Integer.valueOf(4));
		client.sendMessage("test5", Float.valueOf(5.0f));
		client.sendMessage("test6", Long.valueOf(6));
		client.sendMessage("test7", Double.valueOf(7));
		client.sendMessage("test8", new TestMessageType(8, messageText, null));
		messageLatch.await(24, TimeUnit.HOURS);

		assertEquals(messagesNumber, messages.size());
		if (messages.get(0) instanceof Byte) {
			assertEquals(((Byte) messages.get(0)).byteValue(), 1);
		} else {
			fail("Expected Byte!");
		}
		if (messages.get(1) instanceof Short) {
			assertEquals(((Short) messages.get(1)).shortValue(), 2);
		} else {
			fail("Expected Short!");
		}
		if (messages.get(2) instanceof Character) {
			assertEquals(((Character) messages.get(2)).charValue(), 3);
		} else {
			fail("Expected Character!");
		}
		if (messages.get(3) instanceof Integer) {
			assertEquals(((Integer) messages.get(3)).intValue(), 4);
		} else {
			fail("Expected Integer!");
		}
		if (messages.get(4) instanceof Float) {
			assertEquals(((Float) messages.get(4)).floatValue(), 5.0f, 0.000001);
		} else {
			fail("Expected Float!");
		}
		if (messages.get(5) instanceof Long) {
			assertEquals(((Long) messages.get(5)).longValue(), 6);
		} else {
			fail("Expected Long!");
		}
		if (messages.get(6) instanceof Double) {
			assertEquals(((Double) messages.get(6)).doubleValue(), 7.0, 0.000001);
		} else {
			fail("Expected Double!");
		}
		if (messages.get(7) instanceof TestMessageType) {
			TestMessageType message = (TestMessageType) messages.get(7);
			assertEquals(message.getNumber(), 8);
			assertEquals(message.getText(), messageText);
		} else {
			fail("Expected TestMessageType!");
		}
	}

	public static RoboContext createTestContext() {
		RoboTestContext testContext = new RoboTestContext("TestContext", ConfigurationFactory.createEmptyConfiguration());
		Configuration configuration = new ConfigurationBuilder().addString("name", "Test").addString("description", "Lalalala").build();
		testContext.addRef(new RoboTestReference("test", configuration));
		return testContext;
	}
}
