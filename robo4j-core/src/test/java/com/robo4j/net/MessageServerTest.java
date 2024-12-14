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
package com.robo4j.net;

import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.configuration.ConfigurationFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class MessageServerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageServerTest.class);
    private static final int TIMEOUT_SEC = 30;
    private static final String CONST_MY_UUID = "myuuid";
    private static final String PROPERTY_SERVER_NAME = "ServerName";
    private static final int SERVER_LISTEN_DELAY_MILLIS = 250;
    private static final String LOCALHOST_VALUE = "localhost";
    private static final String MESSAGE_SERVER_NAME = "Server Name";
    private volatile Exception exception = null;

    @Test
    void clientServerMessagePassingTest() throws Exception {

        final var messageCache = new ArrayList<>();
        final var messagesLatch = new CountDownLatch(3);

        var messageServerConfig = new ConfigurationBuilder()
                .addString(PROPERTY_SERVER_NAME, MESSAGE_SERVER_NAME)
                .addString(MessageServer.KEY_HOST_NAME, LOCALHOST_VALUE)
                .build();
        var messageServer = new MessageServer((uuid, id, message) -> {
            printInfo(uuid, id, message);
            messageCache.add(String.valueOf(message));
            messagesLatch.countDown();
        }, messageServerConfig);

        var serverListenerThread = new Thread(() -> {
            try {
                messageServer.start();
            } catch (IOException e) {
                exception = e;
                fail(e.getMessage());
            }
        }, "Server Listener");
        serverListenerThread.setDaemon(true);
        serverListenerThread.start();
        for (int i = 0; i < 10; i++) {
            if (messageServer.getListeningPort() == 0) {
                Thread.sleep(SERVER_LISTEN_DELAY_MILLIS);
            } else {
                break;
            }
        }

        var messageReceiverConfig = ConfigurationFactory.createEmptyConfiguration();
        var messageReceiver = new MessageClient(messageServer.getListeningURI(), CONST_MY_UUID, messageReceiverConfig);
        if (exception != null) {
            throw exception;
        }

        List<String> testMessage = getOrderedTestMessage("My First Little Message!", "My Second Little Message!",
                "My Third Little Message!");
        messageReceiver.connect();
        for (String message : testMessage) {
            messageReceiver.sendMessage("test", message);
        }

        var receivedMessages = messagesLatch.await(TIMEOUT_SEC, TimeUnit.SECONDS);

        assertTrue(receivedMessages);
        assertEquals(testMessage.size(), messageCache.size());
        assertArrayEquals(testMessage.toArray(), messageCache.toArray());
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

        final var serverConfig = new ConfigurationBuilder()
                .addString(PROPERTY_SERVER_NAME, MESSAGE_SERVER_NAME)
                .addString(MessageServer.KEY_HOST_NAME, LOCALHOST_VALUE)
                .build();
        var messageServer = new MessageServer((uuid, id, message) -> {
            printInfo(uuid, id, message);
            messages.add(message);
            messageLatch.countDown();
        }, serverConfig);

        Thread t = new Thread(() -> {
            try {
                messageServer.start();
            } catch (IOException e) {
                exception = e;
                fail(e.getMessage());
            }
        }, "Server Listener");
        t.setDaemon(true);
        t.start();
        for (int i = 0; i < 10; i++) {
            if (messageServer.getListeningPort() == 0) {
                Thread.sleep(250);
            } else {
                break;
            }
        }

        Configuration clientConfig = ConfigurationFactory.createEmptyConfiguration();
        MessageClient client = new MessageClient(messageServer.getListeningURI(), CONST_MY_UUID, clientConfig);
        if (exception != null) {
            throw exception;
        }
        client.connect();

        // TODO : review boxing
        client.sendMessage("test1", (byte) 1);
        client.sendMessage("test2", (short) 2);
        client.sendMessage("test3", Character.valueOf((char) 3));
        client.sendMessage("test4", 4);
        client.sendMessage("test5", 5.0f);
        client.sendMessage("test6", 6L);
        client.sendMessage("test7", 7d);
        client.sendMessage("test8", new TestMessageType(8, messageText, null));
        messageLatch.await(TIMEOUT_SEC, TimeUnit.SECONDS);

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

    private static void printInfo(String uuid, String id, Object message) {
        LOGGER.info("Got uuid: {} got id:{} message:{}", uuid, id, message);
    }
}
