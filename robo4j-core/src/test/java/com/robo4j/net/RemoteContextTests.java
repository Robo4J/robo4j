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

import com.robo4j.ConfigurationException;
import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.StringConsumer;
import com.robo4j.StringProducerRemote;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.util.SystemUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Note that on Mac OS X, it seems the easiest way to get this test to run is to
 * set -Djava.net.preferIPv4Stack=true.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

public class RemoteContextTests {
	private static final String ACK_CONSUMER = "ackConsumer";
	private static final String REMOTE_UNIT_EMITTER = "remoteEmitter";
	private static final int NUMBER_ITERATIONS = 10;
	private static final String REMOTE_CONTEXT_RECEIVER = "remoteReceiver";
	public static final String UNIT_STRING_CONSUMER = "stringConsumer";
	public static final String CONTEXT_ID_REMOTE_RECEIVER = "remoteReceiver";

	@Test
	public void testDiscoveryOfDiscoveryEnabledRoboContext() throws RoboBuilderException, IOException {
		RoboBuilder builder = new RoboBuilder(SystemUtil.getInputStreamByResourceName("testDiscoverableSystem.xml"));
		RoboContext ctx = builder.build();
		ctx.start();

		final LookupService service = LookupServiceTests.getLookupService(new LocalLookupServiceImpl());

		service.start();
		for (int i = 0; i < NUMBER_ITERATIONS && (service.getDescriptor("6") == null); i++) {
			SystemUtil.sleep(200);
		}

		Assert.assertTrue(service.getDiscoveredContexts().size() > 0);
		RoboContextDescriptor descriptor = service.getDescriptor("6");
		Assert.assertEquals(descriptor.getMetadata().get("name"), "Caprica");
		Assert.assertEquals(descriptor.getMetadata().get("class"), "Cylon");
		ctx.shutdown();
	}

	@Test
	public void testMessageToDiscoveredContext() throws RoboBuilderException, IOException, ConfigurationException {
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
			SystemUtil.sleep(200);
		}
		Assert.assertTrue(service.getDiscoveredContexts().size() > 0);
		RoboContextDescriptor descriptor = service.getDescriptor("7");
		Assert.assertNotNull(descriptor);

		builder = new RoboBuilder(
				RemoteContextTests.class.getClassLoader().getResourceAsStream("testMessageEmitterSystem_10.xml"));
		RemoteStringProducer remoteStringProducer = new RemoteStringProducer(builder.getContext(), REMOTE_UNIT_EMITTER);
		remoteStringProducer.initialize(getEmitterConfiguration("7", ACK_CONSUMER));
		builder.add(remoteStringProducer);
		RoboContext emitterContext = builder.build();
		localLookup.addContext(emitterContext);

		emitterContext.start();

		remoteStringProducer.sendMessage("sendRandomMessage");
		for (int i = 0; i < NUMBER_ITERATIONS && consumer.getReceivedMessages().size() == 0; i++) {
			SystemUtil.sleep(200);
		}

		Assert.assertTrue(consumer.getReceivedMessages().size() > 0);
		System.out.println("Got messages: " + consumer.getReceivedMessages());
		emitterContext.shutdown();
		receiverCtx.shutdown();
	}

	@Test
	public void testMessageIncludingReferenceToDiscoveredContext()
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
			SystemUtil.sleep(200);
		}
		Assert.assertTrue(service.getDiscoveredContexts().size() > 0);
		RoboContextDescriptor descriptor = service.getDescriptor("9");
		Assert.assertNotNull(descriptor);

		builder = new RoboBuilder(SystemUtil.getInputStreamByResourceName("testMessageEmitterSystem_8.xml"));
		RemoteTestMessageProducer remoteTestMessageProducer = new RemoteTestMessageProducer(builder.getContext(),
				REMOTE_UNIT_EMITTER);
		remoteTestMessageProducer.initialize(getEmitterConfiguration("9", ACK_CONSUMER));
		builder.add(remoteTestMessageProducer);
		RoboContext emitterContext = builder.build();
		localLookup.addContext(emitterContext);

		emitterContext.start();

		remoteTestMessageProducer.sendMessage("sendMessage");
		for (int i = 0; i < NUMBER_ITERATIONS && consumer.getReceivedMessages().size() == 0; i++) {
			SystemUtil.sleep(200);
		}

		Assert.assertTrue(consumer.getReceivedMessages().size() > 0);
		System.out.println("Got messages: " + consumer.getReceivedMessages());

		Assert.assertTrue(remoteTestMessageProducer.getAckCount() > 0);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMessageToDiscoveredContextAndReferenceToDiscoveredContext() throws Exception {
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
		Assert.assertTrue(service.getDiscoveredContexts().size() > 0);
		Assert.assertNotNull(contextDescriptor);

		// build the producer system
		RoboContext producerEmitterSystem = buildEmitterContext(TestMessageType.class, ACK_CONSUMER,
				REMOTE_UNIT_EMITTER);
		producerEmitterSystem.start();
		localLookup.addContext(producerEmitterSystem);

		RoboReference<String> remoteTestMessageProducer = producerEmitterSystem.getReference(REMOTE_UNIT_EMITTER);
		remoteTestMessageProducer.sendMessage(RemoteTestMessageProducer.ATTR_SEND_MESSAGE);
		CountDownLatch ackConsumerCountDownLatch = ackConsumer
				.getAttribute(AckingStringConsumer.DESCRIPTOR_ACK_LATCH).get();
		ackConsumerCountDownLatch.await();

		List<TestMessageType> receivedMessages = (List<TestMessageType>) ackConsumer
				.getAttribute(AckingStringConsumer.DESCRIPTOR_MESSAGES).get();
		Assert.assertTrue(receivedMessages.size() > 0);

		CountDownLatch producerCountDownLatch = remoteTestMessageProducer
				.getAttribute(RemoteTestMessageProducer.DESCRIPTOR_COUNT_DOWN_LATCH).get();
		producerCountDownLatch.await();
		CountDownLatch producerAckLatch = remoteTestMessageProducer
				.getAttribute(RemoteTestMessageProducer.DESCRIPTOR_ACK_LATCH).get();
		producerAckLatch.await();
		Integer producerAcknowledge = remoteTestMessageProducer
				.getAttribute(RemoteTestMessageProducer.DESCRIPTOR_TOTAL_ACK).get();
		Assert.assertTrue(producerAcknowledge > 0);
	}

	@Ignore
	@Test
	public void startRemoteReceiver() throws Exception {
		buildReceiverSystemStringConsumer();

		final LocalLookupServiceImpl localLookup = new LocalLookupServiceImpl();
		final LookupService service = LookupServiceTests.getLookupService(localLookup);

		LookupServiceProvider.setDefaultLookupService(service);
		service.start();
		System.in.read();
	}

	@Ignore
	@Test
	public void startRemoteSender() throws Exception {
		// Note that all this cludging about with local lookup service
		// implementations etc would normally not be needed.
		// This is just to isolate this test from other tests.
		final LocalLookupServiceImpl localLookup = new LocalLookupServiceImpl();
		final LookupService service = LookupServiceTests.getLookupService(localLookup);

		LookupServiceProvider.setDefaultLookupService(service);
		service.start();

		// context has been discovered
		RoboContextDescriptor descriptor = getRoboContextDescriptor(service, CONTEXT_ID_REMOTE_RECEIVER);
		Assert.assertTrue(service.getDiscoveredContexts().size() > 0);
		Assert.assertNotNull(descriptor);

		// build the producer system
		RoboContext producerEmitterSystem = buildEmitterContext(String.class, UNIT_STRING_CONSUMER,
				REMOTE_UNIT_EMITTER);
		localLookup.addContext(producerEmitterSystem);

		producerEmitterSystem.start();
		RoboReference<String> remoteTestMessageProducer = producerEmitterSystem.getReference(REMOTE_UNIT_EMITTER);

		for (int i = 0; i < 10; i++) {
			remoteTestMessageProducer.sendMessage("REMOTE MESSAGE :" + i);
		}

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
	private RoboContext buildReceiverSystemStringConsumer() throws RoboBuilderException, ConfigurationException {
		RoboBuilder builder = new RoboBuilder(SystemUtil.getInputStreamByResourceName("testRemoteReceiver.xml"));
		Configuration configuration = new ConfigurationBuilder().addInteger("totalNumberMessages", 1).build();
		StringConsumer stringConsumer = new StringConsumer(builder.getContext(), UNIT_STRING_CONSUMER);
		stringConsumer.initialize(configuration);

		builder.add(stringConsumer);
		RoboContext receiverSystem = builder.build();
		receiverSystem.start();
		return receiverSystem;
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
		Configuration configuration = new ConfigurationBuilder().addString("target", target)
				.addString("targetContext", targetContext).addInteger("totalNumberMessages", 1).build();
		return configuration;
	}

}
