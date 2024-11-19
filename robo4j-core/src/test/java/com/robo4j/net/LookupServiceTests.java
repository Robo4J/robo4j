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

    static LookupService getLookupService(LocalLookupServiceImpl localLookupService) {
        return DefaultLookupServiceBuilder.Build()
                .setAddress(DEFAULT_MULTICAST_ADDRESS)
                .setPort(DEFAULT_PORT)
                .setMissedHeartbeatsBeforeRemoval(ALLOWED_HEARTBEAT_MISSES)
                .setLocalContexts(localLookupService)
                .build();
    }

    @Test
    void testEncodeDecode() throws IOException {
        Map<String, String> metadata = new HashMap<>();
        String id = "MyID";
        int heartBeatInterval = 1234;
        metadata.put("name", "Pretty Human Readable Name");
        metadata.put("uri", "robo4j://localhost:12345");
        RoboContextDescriptor descriptor = new RoboContextDescriptor(id, heartBeatInterval, metadata);
        byte[] encodedDescriptor = HearbeatMessageCodec.encode(descriptor);
        RoboContextDescriptor decodedDescriptor = HearbeatMessageCodec.decode(encodedDescriptor);

        assertEquals(descriptor.getId(), decodedDescriptor.getId());
        assertEquals(descriptor.getHeartBeatInterval(), decodedDescriptor.getHeartBeatInterval());
        assertEquals(descriptor.getMetadata(), decodedDescriptor.getMetadata());
    }

    @Test
    void lookupEmitterTest() throws IOException, InterruptedException {
        final var service = getLookupService(new LocalLookupServiceImpl());
        service.start();
        var descriptor = createRoboContextDescriptor();
        var emitter = new ContextEmitter(descriptor, InetAddress.getByName(LookupServiceProvider.DEFAULT_MULTICAST_ADDRESS),
                LookupServiceProvider.DEFAULT_PORT, 250);

        // TODO : review sleep usage
        for (int i = 0; i < 10; i++) {
            emitter.emit();
            Thread.sleep(250);
        }

        var discoveredContexts = service.getDiscoveredContexts();
        var context = service.getContext(descriptor.getId());
        var remoteContext = (ClientRemoteRoboContext) context;

        LOGGER.info("discoveredContexts:{}", discoveredContexts);
        LOGGER.info("Address:{}", remoteContext.getAddress());
        assertNotNull(context);
        assertNotNull(remoteContext.getAddress());
        assertEquals(1, discoveredContexts.size());
    }

    private static RoboContextDescriptor createRoboContextDescriptor() {
        Map<String, String> metadata = new HashMap<>();
        String id = "MyID";
        int heartBeatInterval = 1234;
        metadata.put("name", "Pretty Human Readable Name");
        metadata.put(RoboContextDescriptor.KEY_URI, "robo4j://localhost:12345");
        return new RoboContextDescriptor(id, heartBeatInterval, metadata);
    }

}
