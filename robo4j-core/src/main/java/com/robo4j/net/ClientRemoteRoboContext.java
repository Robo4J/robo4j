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

import com.robo4j.AttributeDescriptor;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationFactory;
import com.robo4j.scheduler.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ClientRemoteRoboContext implements RoboContext {
    private record ClientRemoteRoboReference<T>(String id, MessageClient client) implements RoboReference<T> {
        private static final Logger LOGGER = LoggerFactory.getLogger(ClientRemoteRoboReference.class);


        @Override
        public LifecycleState getState() {
            throw new UnsupportedOperationException("Not supported yet!");
        }

        @Override
        public void sendMessage(Object message) {
            try {
                if (!client.isConnected()) {
                    client.connect();
                }
                client.sendMessage(id, message);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        @Override
        public Class<T> getMessageType() {
            throw new UnsupportedOperationException("Not supported yet!");
        }

        @Override
        public Configuration getConfiguration() {
            throw new UnsupportedOperationException("Not supported yet!");
        }

        @Override
        public <R> Future<R> getAttribute(AttributeDescriptor<R> attribute) {
            throw new UnsupportedOperationException("Not supported yet!");
        }

        @Override
        public Collection<AttributeDescriptor<?>> getKnownAttributes() {
            throw new UnsupportedOperationException("Not supported yet!");
        }

        @Override
        public Future<Map<AttributeDescriptor<?>, Object>> getAttributes() {
            throw new UnsupportedOperationException("Not supported yet!");
        }

    }

    private static MessageClient initializeClient(RoboContextDescriptorEntry descriptorEntry) {
        return new MessageClient(URI.create(descriptorEntry.descriptor.getMetadata().get(RoboContextDescriptor.KEY_URI)),
                descriptorEntry.descriptor.getId(), ConfigurationFactory.createEmptyConfiguration());
    }

    private final RoboContextDescriptorEntry descriptorEntry;
    private final MessageClient client;

    ClientRemoteRoboContext(RoboContextDescriptorEntry descriptorEntry) {
        this.descriptorEntry = descriptorEntry;
        client = initializeClient(descriptorEntry);
    }

    @Override
    public LifecycleState getState() {
        return null;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void start() {
    }

    @Override
    public <T> RoboReference<T> getReference(String id) {
        return new ClientRemoteRoboReference<>(id, client);
    }

    @Override
    public Collection<RoboReference<?>> getUnits() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Scheduler getScheduler() {
        throw new UnsupportedOperationException("Accessing the Scheduler remotely is not supported. Use the local scheduler.");
    }

    @Override
    public String getId() {
        return descriptorEntry.descriptor.getId();
    }

    public InetAddress getAddress() {
        return descriptorEntry.address;
    }

    @Override
    public Configuration getConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }
}
