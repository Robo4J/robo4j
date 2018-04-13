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
import com.robo4j.StringConsumer;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationFactory;
import com.robo4j.util.SystemUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

public class RemoteContextTests {
	private static final String ACK_CONSUMER = "ackConsumer";
	private static final String REMOTE_EMITTER = "remoteEmitter";
	public static final int NUMBER_ITERATIONS = 10;

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

		// Note that all this cludging about with local lookup service implementations
		// etc would normally not be needed.
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
				RemoteContextTests.class.getClassLoader().getResourceAsStream("testMessageEmitterSystem.xml"));
		RemoteStringProducer remoteStringProducer = new RemoteStringProducer(builder.getContext(), REMOTE_EMITTER);
		remoteStringProducer.initialize(getEmitterConfiguration("7"));
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

		// Note that all this cludging about with local lookup service implementations
		// etc would normally not be needed.
		// This is just to isolate this test from other tests.
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

		builder = new RoboBuilder(SystemUtil.getInputStreamByResourceName("testTestMessageEmitterSystem.xml"));
		RemoteTestMessageProducer remoteTestMessageProducer = new RemoteTestMessageProducer(builder.getContext(),
				REMOTE_EMITTER);
		remoteTestMessageProducer.initialize(getEmitterConfiguration("9"));
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
	public void testMessageToDiscoveredContextAndReferenceToDiscoveredContext() throws Exception {

		RoboBuilder builder = new RoboBuilder(SystemUtil.getInputStreamByResourceName("testRemoteReceiver.xml"));
		Configuration configuration = ConfigurationFactory.createEmptyConfiguration();
		configuration.setInteger("totalNumberMessages", 1);
		AckingStringConsumer ackConsumer = new AckingStringConsumer(builder.getContext(), ACK_CONSUMER);
		ackConsumer.initialize(configuration);

		builder.add(ackConsumer);
		RoboContext receiverContext = builder.build();
		receiverContext.start();

		// Note that all this cludging about with local lookup service implementations
		// etc would normally not be needed.
		// This is just to isolate this test from other tests.
		final LocalLookupServiceImpl localLookup = new LocalLookupServiceImpl();
		final LookupService service = LookupServiceTests.getLookupService(localLookup);

		LookupServiceProvider.setDefaultLookupService(service);
		service.start();

		// context has been discovered
		CountDownLatch lookupServiceLatch = new CountDownLatch(1);
		receiverContext.getScheduler().execute(() -> {
			while (service.getDescriptor("remoteReceiver") == null) {

			}
			lookupServiceLatch.countDown();
		});
		lookupServiceLatch.await();

		Assert.assertTrue(service.getDiscoveredContexts().size() > 0);
		RoboContextDescriptor descriptor = service.getDescriptor("remoteReceiver");
		Assert.assertNotNull(descriptor);

		builder = new RoboBuilder(SystemUtil.getInputStreamByResourceName("testTestMessageEmitterSystem.xml"));
		RemoteTestMessageProducer remoteTestMessageProducer = new RemoteTestMessageProducer(builder.getContext(),
				REMOTE_EMITTER);
		remoteTestMessageProducer.initialize(getEmitterConfiguration("remoteReceiver"));
		builder.add(remoteTestMessageProducer);
		RoboContext emitterContext = builder.build();
		localLookup.addContext(emitterContext);

		emitterContext.start();

		remoteTestMessageProducer.sendMessage("sendMessage");
		CountDownLatch consumerCountDownLatch = ackConsumer
				.getAttribute(AckingStringConsumer.DESCRIPTOR_COUNT_DOWN_LATCH).get();
		consumerCountDownLatch.await();

		Assert.assertTrue(ackConsumer.getReceivedMessages().size() > 0);
		System.out.println("Got messages: " + ackConsumer.getReceivedMessages());

		CountDownLatch producerCountDownLatch = remoteTestMessageProducer
				.getAttribute(AckingStringConsumer.DESCRIPTOR_COUNT_DOWN_LATCH).get();
		producerCountDownLatch.await();
		Assert.assertTrue(remoteTestMessageProducer.getAckCount() > 0);

	}

	private Configuration getEmitterConfiguration(String targetContext) {
		Configuration configuration = ConfigurationFactory.createEmptyConfiguration();
		configuration.setString("target", ACK_CONSUMER);
		configuration.setString("targetContext", targetContext);
		configuration.setInteger("totalNumberMessages", 1);
		return configuration;
	}

}
