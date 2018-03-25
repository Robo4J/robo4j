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

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

public class RemoteContextTests {
	private static final String CONSUMER = "consumer";
	private static final String REMOTE_EMITTER = "remoteEmitter";
	private static final float ALLOWED_HEARTBEAT_MISSES = 22f;

	@Test
	public void testDiscoveryOfDiscoveryEnabledRoboContext() throws RoboBuilderException, IOException {
		RoboBuilder builder = new RoboBuilder(RemoteContextTests.class.getClassLoader().getResourceAsStream("testDiscoverableSystem.xml"));
		RoboContext ctx = builder.build();
		ctx.start();
		LookupService service = new LookupServiceImpl(LookupServiceProvider.DEFAULT_MULTICAST_ADDRESS, LookupServiceProvider.DEFAULT_PORT,
				ALLOWED_HEARTBEAT_MISSES, new LocalLookupServiceImpl());
		service.start();
		for (int i = 0; i < 10 && (service.getDescriptor("6") == null); i++) {
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
				RemoteContextTests.class.getClassLoader().getResourceAsStream("testRemoteMessageReceiverSystem.xml"));
		StringConsumer consumer = new StringConsumer(builder.getContext(), CONSUMER);
		builder.add(consumer);
		RoboContext receiverCtx = builder.build();
		receiverCtx.start();

		
		// Note that all this cludging about with local lookup service implementations etc would normally not be needed.
		// This is just to isolate this test from other tests.
		LocalLookupServiceImpl localLookup = new LocalLookupServiceImpl();		
		LookupService service = new LookupServiceImpl(LookupServiceProvider.DEFAULT_MULTICAST_ADDRESS, LookupServiceProvider.DEFAULT_PORT,
				ALLOWED_HEARTBEAT_MISSES, localLookup);
		LookupServiceProvider.setDefaultLookupService(service);
		service.start();

		for (int i = 0; i < 10 && (service.getDescriptor("7") == null); i++) {
			SystemUtil.sleep(200);
		}
		Assert.assertTrue(service.getDiscoveredContexts().size() > 0);
		RoboContextDescriptor descriptor = service.getDescriptor("7");
		Assert.assertNotNull(descriptor);

		builder = new RoboBuilder(RemoteContextTests.class.getClassLoader().getResourceAsStream("testMessageEmitterSystem.xml"));
		RemoteStringProducer remoteStringProducer = new RemoteStringProducer(builder.getContext(), REMOTE_EMITTER);
		remoteStringProducer.initialize(getEmitterConfiguration("7"));
		builder.add(remoteStringProducer);
		RoboContext emitterContext = builder.build();
		localLookup.addContext(emitterContext);
		
		emitterContext.start();

		remoteStringProducer.sendMessage("sendRandomMessage");
		for (int i = 0; i < 10 && consumer.getReceivedMessages().size() == 0; i++) {
			SystemUtil.sleep(200);
		}

		Assert.assertTrue(consumer.getReceivedMessages().size() > 0);
		System.out.println("Got messages: " + consumer.getReceivedMessages());
		emitterContext.shutdown();
		receiverCtx.shutdown();
	}

	@Test
	public void testMessageIncludingReferenceToDiscoveredContext() throws RoboBuilderException, IOException, ConfigurationException {
		RoboBuilder builder = new RoboBuilder(
				RemoteContextTests.class.getClassLoader().getResourceAsStream("testRemoteMessageReceiverAckSystem.xml"));
		AckingStringConsumer consumer = new AckingStringConsumer(builder.getContext(), CONSUMER);
		builder.add(consumer);
		RoboContext receiverCtx = builder.build();
		receiverCtx.start();

		// Note that all this cludging about with local lookup service implementations etc would normally not be needed.
		// This is just to isolate this test from other tests.
		LocalLookupServiceImpl localLookup = new LocalLookupServiceImpl();		
		LookupService service = new LookupServiceImpl(LookupServiceProvider.DEFAULT_MULTICAST_ADDRESS, LookupServiceProvider.DEFAULT_PORT,
				ALLOWED_HEARTBEAT_MISSES, localLookup);
		LookupServiceProvider.setDefaultLookupService(service);
		service.start();

		for (int i = 0; i < 10 && (service.getDescriptor("9") == null); i++) {
			SystemUtil.sleep(200);
		}
		Assert.assertTrue(service.getDiscoveredContexts().size() > 0);
		RoboContextDescriptor descriptor = service.getDescriptor("9");
		Assert.assertNotNull(descriptor);

		builder = new RoboBuilder(RemoteContextTests.class.getClassLoader().getResourceAsStream("testTestMessageEmitterSystem.xml"));
		RemoteTestMessageProducer remoteTestMessageProducer = new RemoteTestMessageProducer(builder.getContext(), REMOTE_EMITTER);
		remoteTestMessageProducer.initialize(getEmitterConfiguration("9"));
		builder.add(remoteTestMessageProducer);
		RoboContext emitterContext = builder.build();
		localLookup.addContext(emitterContext);
		
		emitterContext.start();

		remoteTestMessageProducer.sendMessage("sendMessage");
		for (int i = 0; i < 10 && consumer.getReceivedMessages().size() == 0; i++) {
			SystemUtil.sleep(200);
		}

		Assert.assertTrue(consumer.getReceivedMessages().size() > 0);
		System.out.println("Got messages: " + consumer.getReceivedMessages());

		Assert.assertTrue(remoteTestMessageProducer.getAckCount() > 0);
	}

	private Configuration getEmitterConfiguration(String targetContext) {
		Configuration configuration = ConfigurationFactory.createEmptyConfiguration();
		configuration.setString("target", CONSUMER);
		configuration.setString("targetContext", targetContext);
		return configuration;
	}

}
