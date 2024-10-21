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

import com.robo4j.AttributeDescriptor;
import com.robo4j.LifecycleState;
import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_HOST;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_SOCKET_PORT;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_TARGET;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_UNIT_PATHS_CONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Dynamic HttpUnit request/method configuration
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
class RoboHttpDynamicTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoboHttpDynamicTests.class);
    private static final int TIMEOUT_MIN = 1;
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
        var mainSystem = getServerRoboSystem(MESSAGES_NUMBER);

        /* system which is testing main system */
        var clientSystem = getClientRoboSystem();

        LOGGER.info("Client system state after start:");
        LOGGER.info(SystemUtil.printStateReport(clientSystem));
        LOGGER.info("Main system state after start:");
        LOGGER.info(SystemUtil.printStateReport(mainSystem));

        /* client system sending a messages to the main system */
        var decoratedProducer = clientSystem.getReference(DECORATED_PRODUCER);
        decoratedProducer.sendMessage(MESSAGES_NUMBER);

        // TODO: review how to receiving attributes
        var countDownLatchDecoratedProducer = getAttributeOrTimeout(decoratedProducer, SocketMessageDecoratedProducerUnit.DESCRIPTOR_MESSAGES_LATCH);
        var messagesProduced = countDownLatchDecoratedProducer.await(TIMEOUT_MIN, TIME_UNIT);
        var stringConsumer = mainSystem.getReference(StringConsumer.NAME);
        var countDownLatch = getAttributeOrTimeout(stringConsumer, StringConsumer.DESCRIPTOR_MESSAGES_LATCH);
        var messagesReceived = countDownLatch.await(TIMEOUT_MIN, TIME_UNIT);
        var receivedMessages = getAttributeOrTimeout(stringConsumer, StringConsumer.DESCRIPTOR_MESSAGES_TOTAL);

        clientSystem.shutdown();
        mainSystem.shutdown();

        LOGGER.info("System is Down!");
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
        LOGGER.info("PingSystem state after start:");
        LOGGER.info(SystemUtil.printStateReport(pingSystemContext));

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
        LOGGER.info("PingSystem state after stop:");
        LOGGER.info(SystemUtil.printStateReport(pingSystemContext));

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
        LOGGER.info(SystemUtil.printSocketEndPoint(result.getReference(ID_HTTP_SERVER),
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

    // TODO: maybe some duplication
    private static <T, R> R getAttributeOrTimeout(RoboReference<T> roboReference, AttributeDescriptor<R> attributeDescriptor) throws InterruptedException, ExecutionException, TimeoutException {
        var attribute = roboReference.getAttribute(attributeDescriptor).get(TIMEOUT_MIN, TimeUnit.MINUTES);
        if (attribute == null) {
            attribute = roboReference.getAttribute(attributeDescriptor).get(TIMEOUT_MIN, TimeUnit.MINUTES);
            LOGGER.error("roboReference:{}, no attribute:{}", roboReference.getId(), attributeDescriptor.getAttributeName());
        }
        return attribute;
    }

}
