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

import com.robo4j.socket.http.test.units.config.SocketMessageDecoratedProducerUnit;
import com.robo4j.socket.http.test.units.config.StringConsumer;
import com.robo4j.util.SystemUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * testing http method GET with response
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */

class RoboHttpClientWithResponseTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoboHttpClientWithResponseTests.class);
    private static final int TIMEOUT = 20;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    private static final Integer MAX_NUMBER = 20;
    // private static final String ROBO_SYSTEM_DESC =
    // "[{\"id\":\"stringConsumer\",\"state\":\"STARTED\"},{\"id\":\"httpServer\",\"state\":\"STARTED\"}]";

    @Test
    void simpleRoboSystemGetRequestTest() throws Exception {

        var producerSystem = RoboContextUtils
                .loadRoboContextByXml("robo_http_client_request_producer_text.xml");
        var consumerSystem = RoboContextUtils
                .loadRoboContextByXml("robo_http_client_request_consumer_text.xml");

        consumerSystem.start();
        producerSystem.start();

        LOGGER.info("consumer: State after start:");
        LOGGER.info(SystemUtil.printStateReport(consumerSystem));
        LOGGER.info("producer: State after start:");
        LOGGER.info(SystemUtil.printStateReport(producerSystem));

        var decoratedProducer = producerSystem.getReference("decoratedProducer");
        var producerSetupLatch = decoratedProducer
                .getAttribute(SocketMessageDecoratedProducerUnit.DESCRIPTOR_SETUP_LATCH).get();
        decoratedProducer.sendMessage(MAX_NUMBER);
        var producerConfigured = producerSetupLatch.await(TIMEOUT, TIME_UNIT);
        var producerLatch = decoratedProducer
                .getAttribute(SocketMessageDecoratedProducerUnit.DESCRIPTOR_MESSAGES_LATCH).get();
        var messagesSent = producerLatch.await(TIMEOUT, TIME_UNIT);

        var producerStringConsumer = producerSystem.getReference(StringConsumer.NAME);
        var messagesLatchStringConsumer = producerStringConsumer
                .getAttribute(StringConsumer.DESCRIPTOR_MESSAGES_LATCH).get();
        var messagesReceived = messagesLatchStringConsumer.await(TIMEOUT, TIME_UNIT);

        var totalNumber = producerStringConsumer.getAttribute(StringConsumer.DESCRIPTOR_MESSAGES_TOTAL).get();

        assertTrue(producerConfigured);
        assertTrue(messagesSent);
        assertTrue(messagesReceived);
        assertEquals(MAX_NUMBER, totalNumber);

        producerSystem.shutdown();
        consumerSystem.shutdown();
    }

}
