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

import com.robo4j.*;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.message.HttpRequestDenominator;
import com.robo4j.socket.http.test.units.config.HttpCommandTestController;
import com.robo4j.socket.http.test.units.config.SocketMessageDecoratedProducerUnit;
import com.robo4j.socket.http.test.units.config.StringConsumer;
import com.robo4j.socket.http.units.HttpClientUnit;
import com.robo4j.socket.http.units.HttpServerUnit;
import com.robo4j.socket.http.util.HttpPathConfigJsonBuilder;
import com.robo4j.util.SystemUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.robo4j.socket.http.util.RoboHttpUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Dynamic HttpUnit request/method configuration
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
class RoboHttpDynamicTests {

    private static final int TIMEOUT = 20;
    private static final TimeUnit TIME_UNIT = TimeUnit.HOURS;
    private static final String ID_HTTP_SERVER = "http";
    private static final int PORT = 8025;
    private static final String ID_CLIENT_UNIT = "httpClient";
    private static final String ID_TARGET_UNIT = "controller";
    private static final int MESSAGES_NUMBER = 42;
    private static final String HOST_SYSTEM = "localhost";
    static final String JSON_STRING = "{\"value\":\"stop\"}";
    private static final String DECORATED_PRODUCER = "decoratedProducer";

    /**
     * Motivation Client system is sending messages to the main system over HTTP
     * Main System receives desired number of messages.
     * <p>
     * Values are requested by Attributes
     *
     * @throws Exception exception
     */
    @Test
    void simpleHttpNonUnitTest() throws Exception {

        /* tested system configuration */
        RoboContext mainSystem = getServerRoboSystem(MESSAGES_NUMBER);

        /* system which is testing main system */
        RoboContext clientSystem = getClientRoboSystem();

        System.out.println("Client system state after start:");
        System.out.println(SystemUtil.printStateReport(clientSystem));

        System.out.println("Main system state after start:");
        System.out.println(SystemUtil.printStateReport(mainSystem));

        /* client system sending a messages to the main system */
        RoboReference<Object> decoratedProducer = clientSystem.getReference(DECORATED_PRODUCER);
        decoratedProducer.sendMessage(MESSAGES_NUMBER);

        // TODO: review how to receiving attributes
        CountDownLatch countDownLatchDecoratedProducer = getAttributeOrTimeout(decoratedProducer, SocketMessageDecoratedProducerUnit.DESCRIPTOR_MESSAGES_LATCH);
        var messagesProduced = countDownLatchDecoratedProducer.await(TIMEOUT, TIME_UNIT);

        final RoboReference<String> stringConsumer = mainSystem.getReference(StringConsumer.NAME);
        final CountDownLatch countDownLatch = getAttributeOrTimeout(stringConsumer, StringConsumer.DESCRIPTOR_MESSAGES_LATCH);
        var messagesReceived = countDownLatch.await(TIMEOUT, TIME_UNIT);
        final int receivedMessages = getAttributeOrTimeout(stringConsumer, StringConsumer.DESCRIPTOR_MESSAGES_TOTAL);

        clientSystem.shutdown();
        mainSystem.shutdown();

        System.out.println("System is Down!");
        assertTrue(messagesProduced);
        assertTrue(messagesReceived);
        assertNotNull(mainSystem.getUnits());
        assertEquals(MESSAGES_NUMBER, receivedMessages, "wrong received messages");
    }

    /**
     * testing ping external system
     *
     * @throws Exception exception
     */
    @Disabled("intent to run manual")
    @Test
    void pingExternalSystem() throws Exception {
        RoboBuilder pingSystemBuilder = getHttpClientRobotBuilder("127.0.0.1", 8080);

        pingSystemBuilder.add(StringConsumer.class, StringConsumer.NAME);

        RoboContext pingSystemContext = pingSystemBuilder.build();
        pingSystemContext.start();
        System.out.println("PingSystem state after start:");
        System.out.println(SystemUtil.printStateReport(pingSystemContext));

        RoboReference<HttpDecoratedRequest> httpClient = pingSystemContext.getReference(ID_CLIENT_UNIT);

        Thread.sleep(1000);
        for (int i = 0; i < 1; i++) {
            HttpRequestDenominator denominator = new HttpRequestDenominator(HttpMethod.GET, "/noparams",
                    HttpVersion.HTTP_1_1);
            HttpDecoratedRequest request = new HttpDecoratedRequest(denominator);
            request.addCallback(StringConsumer.NAME);
            httpClient.sendMessage(request);
        }
        Thread.sleep(1000);
        pingSystemContext.stop();
        System.out.println("PingSystem state after stop:");
        System.out.println(SystemUtil.printStateReport(pingSystemContext));

    }

    // Private Methods
    private RoboContext getServerRoboSystem(int totalMessageNumber) throws Exception {
        /* tested system configuration */
        RoboBuilder builder = new RoboBuilder();

        Configuration config = new ConfigurationBuilder().addInteger(PROPERTY_SOCKET_PORT, PORT)
                .addString("packages", "com.robo4j.socket.http.test.units.config.codec").addString(PROPERTY_UNIT_PATHS_CONFIG,
                        HttpPathConfigJsonBuilder.Builder().addPath(ID_TARGET_UNIT, HttpMethod.POST).build())
                .build();
        builder.add(HttpServerUnit.class, config, ID_HTTP_SERVER);

        config = new ConfigurationBuilder().addString(PROPERTY_TARGET, StringConsumer.NAME).build();
        builder.add(HttpCommandTestController.class, config, ID_TARGET_UNIT);

        config = new ConfigurationBuilder().addInteger(StringConsumer.PROP_TOTAL_NUMBER_MESSAGES, totalMessageNumber)
                .build();
        builder.add(StringConsumer.class, config, StringConsumer.NAME);

        RoboContext result = builder.build();
        assertNotNull(result.getUnits());
        assertEquals(3, result.getUnits().size());
        assertEquals(LifecycleState.INITIALIZED, result.getReference(ID_HTTP_SERVER).getState());
        assertEquals(LifecycleState.INITIALIZED, result.getState());

        result.start();
        System.out.println(SystemUtil.printSocketEndPoint(result.getReference(ID_HTTP_SERVER),
                result.getReference(ID_TARGET_UNIT)));
        return result;
    }

    private RoboBuilder getHttpClientRobotBuilder(String host, int port) throws Exception {
        /* system which is testing main system */
        RoboBuilder result = new RoboBuilder();

        Configuration config = new ConfigurationBuilder().addString(PROPERTY_HOST, host)
                .addInteger(PROPERTY_SOCKET_PORT, port).build();
        result.add(HttpClientUnit.class, config, ID_CLIENT_UNIT);
        return result;
    }

    private RoboContext getClientRoboSystem() throws Exception {
        /* system which is testing main system */
        RoboBuilder builder = getHttpClientRobotBuilder(HOST_SYSTEM, PORT);

        Configuration config = new ConfigurationBuilder().addString(PROPERTY_TARGET, ID_CLIENT_UNIT)
                .addString(PROPERTY_UNIT_PATHS_CONFIG,
                        "[{\"roboUnit\":\"" + ID_TARGET_UNIT + "\",\"method\":\"POST\"}]")
                .addString("message", JSON_STRING).build();
        builder.add(SocketMessageDecoratedProducerUnit.class, config, DECORATED_PRODUCER);

        RoboContext result = builder.build();
        result.start();
        return result;
    }

    private static <T, R> R getAttributeOrTimeout(RoboReference<T> roboReference, AttributeDescriptor<R> attributeDescriptor) throws InterruptedException, ExecutionException, TimeoutException {
        var attribute = roboReference.getAttribute(attributeDescriptor).get(TIMEOUT, TimeUnit.MINUTES);
        if (attribute == null) {
            SimpleLoggingUtil.error(RoboHttpDynamicTests.class, "roboReference:" + roboReference.getId() + ", no attribute:" + attributeDescriptor.getAttributeName());
            attribute = roboReference.getAttribute(attributeDescriptor).get(TIMEOUT, TimeUnit.MINUTES);
        }
        return attribute;
    }

}
