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

import com.robo4j.ConfigurationException;
import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.units.StringConsumer;
import com.robo4j.units.StringProducerRemote;
import com.robo4j.util.SystemUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Note that on Mac OS X, it seems the easiest way to get this test to run is to
 * set -Djava.net.preferIPv4Stack=true.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

class RemoteContextTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteContextTests.class);
    private static final int TIMEOUT_SEC = 30;
    private static final int LOOKUP_DELAY_MILLIS = 100;
    private static final String ACK_CONSUMER = "ackConsumer";
    private static final String REMOTE_UNIT_EMITTER = "remoteEmitter";
    private static final int NUMBER_ITERATIONS = 10;
    private static final String REMOTE_CONTEXT_RECEIVER = "remoteReceiver";
    private static final String UNIT_STRING_CONSUMER = "stringConsumer";
    private static final String CONTEXT_ID_REMOTE_RECEIVER = "remoteReceiver";

    @Test
    void discoveryOfDiscoveryEnabledRoboContextTest() throws RoboBuilderException, IOException {
        var expectedMetaDataName = "Caprica";
        var expectedMetaDataClass = "Cylon";

        RoboBuilder builder = new RoboBuilder(SystemUtil.getInputStreamByResourceName("testDiscoverableSystem.xml"));
        RoboContext ctx = builder.build();
        ctx.start();

        final LookupService service = LookupServiceTests.getLookupService(new LocalLookupServiceImpl());

        service.start();
        for (int i = 0; i < NUMBER_ITERATIONS && (service.getDescriptor("6") == null); i++) {
            SystemUtil.sleep(LOOKUP_DELAY_MILLIS);
        }
        RoboContextDescriptor descriptor = service.getDescriptor("6");

        assertFalse(service.getDiscoveredContexts().isEmpty());
        assertEquals(expectedMetaDataName, descriptor.getMetadata().get("name"));
        assertEquals(expectedMetaDataClass, descriptor.getMetadata().get("class"));
        ctx.shutdown();
    }

    // TODO : review a test structure, thread sleep
    @Test
    void messageToDiscoveredContextTest() throws RoboBuilderException, IOException, ConfigurationException {
        RoboBuilder builder = new RoboBuilder(
                SystemUtil.getInputStreamByResourceName("testRemoteMessageReceiverSystem.xml"));
        StringConsumer consumer = new StringConsumer(builder.getContext(), ACK_CONSUMER);
        builder.add(consumer);
        RoboContext receiverCtx = builder.build();
        receiverCtx.start();

        // Note that all this cludging about with local lookup service
        // implementations etc would normally not be needed.
        // This is just to isolate this test from other tests.
        final LocalLookupServiceImpl localLookup = new LocalLookupServiceImpl();
        final LookupService service = LookupServiceTests.getLookupService(localLookup);

        LookupServiceProvider.setDefaultLookupService(service);
        service.start();

        for (int i = 0; i < NUMBER_ITERATIONS && (service.getDescriptor("7") == null); i++) {
            SystemUtil.sleep(LOOKUP_DELAY_MILLIS);
        }
        assertFalse(service.getDiscoveredContexts().isEmpty());
        RoboContextDescriptor descriptor = service.getDescriptor("7");
        assertNotNull(descriptor);

        builder = new RoboBuilder(
                RemoteContextTests.class.getClassLoader().getResourceAsStream("testMessageEmitterSystem_10.xml"));
        RemoteStringProducer remoteStringProducer = new RemoteStringProducer(builder.getContext(), REMOTE_UNIT_EMITTER);
        remoteStringProducer.initialize(getEmitterConfiguration("7", ACK_CONSUMER));
        builder.add(remoteStringProducer);
        RoboContext emitterContext = builder.build();
        localLookup.addContext(emitterContext);

        emitterContext.start();

        remoteStringProducer.sendMessage("sendRandomMessage");
        for (int i = 0; i < NUMBER_ITERATIONS && consumer.getReceivedMessages().isEmpty(); i++) {
            SystemUtil.sleep(LOOKUP_DELAY_MILLIS);
        }

        printMessagesInfo(consumer.getReceivedMessages());
        assertFalse(consumer.getReceivedMessages().isEmpty());
        emitterContext.shutdown();
        receiverCtx.shutdown();
    }

    @Test
    void messageIncludingReferenceToDiscoveredContextTest()
            throws RoboBuilderException, IOException, ConfigurationException {
        RoboBuilder builder = new RoboBuilder(
                SystemUtil.getInputStreamByResourceName("testRemoteMessageReceiverAckSystem.xml"));
        AckingStringConsumer consumer = new AckingStringConsumer(builder.getContext(), ACK_CONSUMER);
        builder.add(consumer);
        RoboContext receiverCtx = builder.build();
        receiverCtx.start();

        final LocalLookupServiceImpl localLookup = new LocalLookupServiceImpl();
        final LookupService service = LookupServiceTests.getLookupService(localLookup);

        LookupServiceProvider.setDefaultLookupService(service);
        service.start();

        for (int i = 0; i < NUMBER_ITERATIONS && (service.getDescriptor("9") == null); i++) {
            SystemUtil.sleep(LOOKUP_DELAY_MILLIS);
        }
        assertFalse(service.getDiscoveredContexts().isEmpty());
        RoboContextDescriptor descriptor = service.getDescriptor("9");
        assertNotNull(descriptor);

        builder = new RoboBuilder(SystemUtil.getInputStreamByResourceName("testMessageEmitterSystem_8.xml"));
        RemoteTestMessageProducer remoteTestMessageProducer = new RemoteTestMessageProducer(builder.getContext(),
                REMOTE_UNIT_EMITTER);
        remoteTestMessageProducer.initialize(getEmitterConfiguration("9", ACK_CONSUMER));
        builder.add(remoteTestMessageProducer);
        RoboContext emitterContext = builder.build();
        localLookup.addContext(emitterContext);

        emitterContext.start();

        remoteTestMessageProducer.sendMessage("sendMessage");
        for (int i = 0; i < NUMBER_ITERATIONS && consumer.getReceivedMessages().isEmpty(); i++) {
            SystemUtil.sleep(200);
        }


        printMessagesInfo(consumer.getReceivedMessages());

        assertFalse(consumer.getReceivedMessages().isEmpty());
        assertTrue(remoteTestMessageProducer.getAckCount() > 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void messageToDiscoveredContextAndReferenceToDiscoveredContextTest() throws Exception {
        RoboContext receiverSystem = buildRemoteReceiverContext(ACK_CONSUMER);
        receiverSystem.start();
        RoboReference<TestMessageType> ackConsumer = receiverSystem.getReference(ACK_CONSUMER);

        // Note that all this cludging about with local lookup service
        // implementations etc would normally not be needed.
        // This is just to isolate this test from other tests.
        final LocalLookupServiceImpl localLookup = new LocalLookupServiceImpl();
        final LookupService service = LookupServiceTests.getLookupService(localLookup);

        LookupServiceProvider.setDefaultLookupService(service);
        service.start();

        // context has been discovered
        RoboContextDescriptor contextDescriptor = getRoboContextDescriptor(service, CONTEXT_ID_REMOTE_RECEIVER);
        assertFalse(service.getDiscoveredContexts().isEmpty());
        assertNotNull(contextDescriptor);

        // build the producer system
        RoboContext producerEmitterSystem = buildEmitterContext(TestMessageType.class, ACK_CONSUMER,
                REMOTE_UNIT_EMITTER);
        producerEmitterSystem.start();
        localLookup.addContext(producerEmitterSystem);

        RoboReference<String> remoteTestMessageProducer = producerEmitterSystem.getReference(REMOTE_UNIT_EMITTER);
        remoteTestMessageProducer.sendMessage(RemoteTestMessageProducer.ATTR_SEND_MESSAGE);
        CountDownLatch ackConsumerCountDownLatch = ackConsumer
                .getAttribute(AckingStringConsumer.DESCRIPTOR_ACK_LATCH).get();
        var ackConsumerLatch = ackConsumerCountDownLatch.await(TIMEOUT_SEC, TimeUnit.SECONDS);

        List<TestMessageType> receivedMessages = (List<TestMessageType>) ackConsumer
                .getAttribute(AckingStringConsumer.DESCRIPTOR_MESSAGES).get();
        assertFalse(receivedMessages.isEmpty());

        CountDownLatch producerCountDownLatch = remoteTestMessageProducer
                .getAttribute(RemoteTestMessageProducer.DESCRIPTOR_COUNT_DOWN_LATCH).get();
        var producedMessagesLatch = producerCountDownLatch.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        CountDownLatch producerAckLatch = remoteTestMessageProducer
                .getAttribute(RemoteTestMessageProducer.DESCRIPTOR_ACK_LATCH).get();
        var producedMessagesAckLatch = producerAckLatch.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        Integer producerAcknowledge = remoteTestMessageProducer
                .getAttribute(RemoteTestMessageProducer.DESCRIPTOR_TOTAL_ACK).get();
        assertTrue(producerAcknowledge > 0);
        assertTrue(ackConsumerLatch);
        assertTrue(producedMessagesLatch);
        assertTrue(producedMessagesAckLatch);
    }

    @Disabled("for individual testing")
    @Test
    void startRemoteReceiverTest() throws Exception {
        buildReceiverSystemStringConsumer();

        final LocalLookupServiceImpl localLookup = new LocalLookupServiceImpl();
        final LookupService service = LookupServiceTests.getLookupService(localLookup);

        LookupServiceProvider.setDefaultLookupService(service);
        service.start();
        System.in.read();
    }

    private <T> RoboContext buildEmitterContext(Class<T> clazz, String target, String unitName) throws Exception {
        RoboBuilder builder = new RoboBuilder(
                SystemUtil.getInputStreamByResourceName("testMessageEmitterSystem_8.xml"));

        if (clazz.equals(String.class)) {
            StringProducerRemote<T> remoteTestMessageProducer = new StringProducerRemote<>(clazz, builder.getContext(),
                    unitName);
            remoteTestMessageProducer.initialize(getEmitterConfiguration(REMOTE_CONTEXT_RECEIVER, target));
            builder.add(remoteTestMessageProducer);
        }
        if (clazz.equals(TestMessageType.class)) {
            RemoteTestMessageProducer remoteTestMessageProducer = new RemoteTestMessageProducer(builder.getContext(),
                    unitName);
            remoteTestMessageProducer.initialize(getEmitterConfiguration(REMOTE_CONTEXT_RECEIVER, target));
            builder.add(remoteTestMessageProducer);
        }
        return builder.build();
    }

    /*
     * Builds a system ready to receive TestMessageType messages, which contains a
     * reference to which the String message "acknowledge" will be sent when a
     * message is received.
     */
    private RoboContext buildRemoteReceiverContext(String name) throws RoboBuilderException, ConfigurationException {
        RoboBuilder builder = new RoboBuilder(SystemUtil.getInputStreamByResourceName("testRemoteReceiver.xml"));
        Configuration configuration = new ConfigurationBuilder()
                .addInteger(AckingStringConsumer.ATTR_TOTAL_NUMBER_MESSAGES, 1).build();
        builder.add(AckingStringConsumer.class, configuration, name);
        return builder.build();
    }

    /*
     * Builds a system ready to receive strings.
     */
    private void buildReceiverSystemStringConsumer() throws RoboBuilderException, ConfigurationException {
        RoboBuilder builder = new RoboBuilder(SystemUtil.getInputStreamByResourceName("testRemoteReceiver.xml"));
        Configuration configuration = new ConfigurationBuilder().addInteger("totalNumberMessages", 1).build();
        StringConsumer stringConsumer = new StringConsumer(builder.getContext(), UNIT_STRING_CONSUMER);
        stringConsumer.initialize(configuration);

        builder.add(stringConsumer);
        RoboContext receiverSystem = builder.build();
        receiverSystem.start();
    }

    private RoboContextDescriptor getRoboContextDescriptor(LookupService service, String remoteContextId) {
        while (service.getDescriptor(remoteContextId) == null) {
            service.getDiscoveredContexts();
        }
        RoboContextDescriptor descriptor = service.getDescriptor(remoteContextId);
        if (descriptor == null) {
            throw new IllegalStateException("not allowed");
        }
        return descriptor;
    }

    private Configuration getEmitterConfiguration(String targetContext, String target) {
        return new ConfigurationBuilder().addString("target", target)
                .addString("targetContext", targetContext).addInteger("totalNumberMessages", 1).build();
    }

    private static <T> void printMessagesInfo(List<T> messages) {
        LOGGER.info("Got messages: {}", messages);
    }
}
