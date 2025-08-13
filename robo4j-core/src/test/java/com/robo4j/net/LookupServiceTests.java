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

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import static com.robo4j.net.LookupServiceProvider.DEFAULT_MULTICAST_ADDRESS;
import static com.robo4j.net.LookupServiceProvider.DEFAULT_PORT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Note that on Mac OS X, it seems the easiest way to get this test to run is to
 * set -Djava.net.preferIPv4Stack=true.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

class LookupServiceTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(LookupServiceTests.class);
    private static final float ALLOWED_HEARTBEAT_MISSES = 22f;
    private static final String TEST_URI = "robo4j://localhost:12345";
    private static final String TEST_NAME = "Pretty Human Readable Name";

    static LookupService getLookupService(LocalLookupServiceImpl localLookupService) {
        return DefaultLookupServiceBuilder.Build()
                .setAddress(DEFAULT_MULTICAST_ADDRESS)
                .setPort(DEFAULT_PORT)
                .setMissedHeartbeatsBeforeRemoval(ALLOWED_HEARTBEAT_MISSES)
                .setLocalContexts(localLookupService)
                .build();
    }

    @Test
    void encodeDecodeTest() {
        var metadata = Map.of(RoboContextDescriptor.KEY_NAME, TEST_NAME, RoboContextDescriptor.KEY_URI, TEST_URI);
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
        final var expectedDiscoveredContexts = 1;
        final var service = getLookupService(new LocalLookupServiceImpl());
        final var descriptor = createRoboContextDescriptor();
        final var heartBeatIntervalMills = 1;
        final var emittedMessagesHearBeatInterval = 1;
        final var emitter = new ContextEmitter(descriptor, InetAddress.getByName(LookupServiceProvider.DEFAULT_MULTICAST_ADDRESS),
                LookupServiceProvider.DEFAULT_PORT, heartBeatIntervalMills);

        service.start();
        // TODO : review sleep usage
        for (int i = 0; i < emittedMessagesHearBeatInterval; i++) {
            emitter.emit();
            Thread.sleep(heartBeatIntervalMills);
        }

        var discoveredContexts = service.getDiscoveredContexts();
        var context = service.getContext(descriptor.getId());
        var remoteContext = (ClientRemoteRoboContext) context;

        LOGGER.info("discoveredContexts:{}", discoveredContexts);
        LOGGER.info("Address:{}", remoteContext.getAddress());
        assertNotNull(context);
        assertNotNull(remoteContext.getAddress());
        assertEquals(expectedDiscoveredContexts, discoveredContexts.size());
    }

    private static RoboContextDescriptor createRoboContextDescriptor() {
        var metadata = new HashMap<String, String>();
        var id = "MyID";
        int heartBeatInterval = 1234;
        metadata.put(RoboContextDescriptor.KEY_NAME, TEST_NAME);
        metadata.put(RoboContextDescriptor.KEY_URI, TEST_URI);
        return new RoboContextDescriptor(id, heartBeatInterval, metadata);
    }

}
