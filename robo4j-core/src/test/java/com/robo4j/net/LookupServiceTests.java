/*
 * Copyright (c) 2014, 2025, Marcus Hirt, Miroslav Wengner
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

import static com.robo4j.net.LookupServiceProvider.DEFAULT_PORT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;

/**
 * Note that on Mac OS X, it seems the easiest way to get this test to run is to set
 * -Djava.net.preferIPv4Stack=true.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

class LookupServiceTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(LookupServiceTests.class);
	private static final float ALLOWED_HEARTBEAT_MISSES = 22f;

	static LookupService getLookupService(LocalLookupServiceImpl localLookupService) throws IOException {
		return DefaultLookupServiceBuilder.Build().setPort(DEFAULT_PORT).setMissedHeartbeatsBeforeRemoval(ALLOWED_HEARTBEAT_MISSES)
				.setLocalContexts(localLookupService).build();
	}

	@Test
	void encodeDecodeTest() {
		var metadata = Map.of("name", "Pretty Human Readable Name", "uri", "robo4j://localhost:12345");
		var id = "MyID";
		var heartBeatInterval = 1234;

		var descriptor = new RoboContextDescriptor(id, heartBeatInterval, metadata);
		var encodedDescriptor = HearbeatMessageCodec.encode(descriptor);
		var decodedDescriptor = HearbeatMessageCodec.decode(encodedDescriptor);

		assertEquals(descriptor.getId(), decodedDescriptor.getId());
		assertEquals(descriptor.getHeartBeatInterval(), decodedDescriptor.getHeartBeatInterval());
		assertEquals(descriptor.getMetadata(), decodedDescriptor.getMetadata());
	}

	@Test
	void lookupEmitterTest() throws IOException, InterruptedException {
		// Skip test if no multicast-capable interfaces are available
		try {
			var family = NetworkUtil.decideFamily(System.getProperty(LookupServiceProvider.PROP_GROUP));
			NetworkUtil.pickMulticastInterface(family);
		} catch (SocketException e) {
			LOGGER.warn("Skipping multicast test - no suitable interface: {}", e.getMessage());
			return;
		}
		final var expectedDiscoveredContexts = 1;
		final var service = getLookupService(new LocalLookupServiceImpl());
		final var descriptor = createRoboContextDescriptor();
		final var heartBeatIntervalMills = 1000; // Use longer interval for more reliable test
		final var emittedMessagesHearBeatInterval = 3; // Send multiple heartbeats

		// Create emitter using configuration approach to ensure consistency
		Configuration emitterConfig = new ConfigurationBuilder().addInteger(ContextEmitter.KEY_PORT, LookupServiceProvider.DEFAULT_PORT)
				.addInteger(ContextEmitter.KEY_HEARTBEAT_INTERVAL, heartBeatIntervalMills).build();
		final var emitter = new ContextEmitter(descriptor, emitterConfig);

		service.start();

		// Give the service a moment to start listening
		Thread.sleep(100);

		// Send multiple heartbeats to ensure discovery
		for (int i = 0; i < emittedMessagesHearBeatInterval; i++) {
			emitter.emit();
			Thread.sleep(100); // Short sleep between emissions
		}

		// Give time for the last heartbeat to be processed
		Thread.sleep(500);

		var discoveredContexts = service.getDiscoveredContexts();
		LOGGER.info("discoveredContexts: {}", discoveredContexts);

		var context = service.getContext(descriptor.getId());
		if (context == null) {
			LOGGER.error("Context not found for ID: {}", descriptor.getId());
			LOGGER.error("Available contexts: {}", discoveredContexts.keySet());
		}

		assertNotNull(context, "Context should be discovered");
		var remoteContext = (ClientRemoteRoboContext) context;

		LOGGER.info("discoveredContexts:{}", discoveredContexts);
		LOGGER.info("Address:{}", remoteContext.getAddress());
		assertNotNull(remoteContext.getAddress());
		assertEquals(expectedDiscoveredContexts, discoveredContexts.size());

		// Cleanup
		service.stop();
	}

	private static RoboContextDescriptor createRoboContextDescriptor() {
		var metadata = new HashMap<String, String>();
		var id = "MyID";
		int heartBeatInterval = 1234;
		metadata.put("name", "Pretty Human Readable Name");
		metadata.put(RoboContextDescriptor.KEY_URI, "robo4j://localhost:12345");
		return new RoboContextDescriptor(id, heartBeatInterval, metadata);
	}

}
