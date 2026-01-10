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

import com.robo4j.RoboContext;
import com.robo4j.net.LocalLookupServiceImpl.LocalRoboContextDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.robo4j.net.HearbeatMessageCodec.notHeartBeatMessage;

/**
 * Package local default implementation of the {@link LookupService}. Will
 * listen on the broadcast address for lookup service related packets and
 * heartbeats. Will automatically remove entries for contexts that have missed
 * enough heartbeats. Note that the entries themselves can have individual
 * settings for the heartbeat.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class LookupServiceImpl implements LookupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LookupServiceImpl.class);
    private static final int PORT_ZERO = 0;
    private static final NetworkInterface LOCAL_NETWORK_INTERFACE_NULL = null;
    // FIXME(marcus/6 Nov 2017): This should be calculated, and used when
    // constructing the packet
    private final static int MAX_PACKET_SIZE = 1500;
    private final String address;
    private final int port;
    private final Map<String, RoboContextDescriptorEntry> entries = new ConcurrentHashMap<>();
    private MulticastSocket socket;
    private Updater currentUpdater;
    private final LocalLookupServiceImpl localContexts;

    private class Updater implements Runnable {
        private final byte[] buffer = new byte[MAX_PACKET_SIZE];
        private final CountDownLatch readyLatch = new CountDownLatch(1);
        private volatile boolean isStopped;

        @Override
        public void run() {
            readyLatch.countDown();
            while (!isStopped) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, MAX_PACKET_SIZE);
                    socket.receive(packet);
                    process(packet);
                } catch (IOException e) {
                    LOGGER.error("Failed to look for lookupservice packets. Lookup service will no longer discover new remote contexts.", e);
                    isStopped = true;
                }
            }
        }

        boolean awaitReady(long timeout, TimeUnit unit) throws InterruptedException {
            return readyLatch.await(timeout, unit);
        }

        private void process(DatagramPacket packet) {
            // First a few quick checks. We want to reject updating anything as
            // early as possible
            if (notHeartBeatMessage(packet.getData())) {
                LOGGER.debug("Non-heartbeat packet sent to LookupService! Ignoring.");
                return;
            }
            if (!HearbeatMessageCodec.isSupportedVersion(packet.getData())) {
                LOGGER.debug("Wrong protocol heartbeat packet sent to LookupService! Ignoring.");
                return;
            }
            String id = parseId(packet.getData());
            if (entries.containsKey(id)) {
                updateEntry(entries.get(id));
            } else {
                addNewEntry(packet);
            }
        }

        private void addNewEntry(DatagramPacket packet) {
            RoboContextDescriptorEntry entry = parse(packet);
            synchronized (LookupServiceImpl.this) {
                entries.put(entry.descriptor.getId(), entry);
            }
        }

        private RoboContextDescriptorEntry parse(DatagramPacket packet) {
            RoboContextDescriptorEntry entry = new RoboContextDescriptorEntry();
            SocketAddress address = packet.getSocketAddress();
            if (address instanceof InetSocketAddress) {
                entry.address = ((InetSocketAddress) address).getAddress();
            }
            entry.descriptor = HearbeatMessageCodec.decode(packet.getData());
            return entry;
        }

        private void updateEntry(RoboContextDescriptorEntry roboContextDescriptorEntry) {
            roboContextDescriptorEntry.lastAccess = System.currentTimeMillis();
        }

        private String parseId(byte[] data) {
            return new String(data, 9, readShort(7, data));
        }

        private int readShort(int i, byte[] data) {
            return (data[i] << 8) + (data[i + 1] & 0xFF);
        }

        public void stop() {
            isStopped = true;
        }
    }

    public LookupServiceImpl(String address, int port, float missedHeartbeatsBeforeRemoval, LocalLookupServiceImpl localContexts) throws SocketException, UnknownHostException {
        this.address = address;
        this.port = port;
        this.localContexts = localContexts;

    }

    @Override
    public synchronized Map<String, RoboContextDescriptor> getDiscoveredContexts() {
        Map<String, RoboContextDescriptor> map = new HashMap<>(entries.size() + localContexts.getDiscoveredContexts().size());
        map.putAll(localContexts.getDiscoveredContexts());
        for (Entry<String, RoboContextDescriptorEntry> entry : entries.entrySet()) {
            map.put(entry.getKey(), entry.getValue().descriptor);
        }
        return Collections.unmodifiableMap(map);
    }

    @Override
    public RoboContext getContext(String id) {
        RoboContextDescriptorEntry entry = entries.get(id);
        if (entry != null) {
            return new ClientRemoteRoboContext(entry);
        } else {
            LocalRoboContextDescriptor localEntry = localContexts.getLocalDescriptor(id);
            return localEntry != null ? localEntry.getContext() : null;
        }
    }

    @Override
    public synchronized void start() throws IOException {
        stop();
        socket = new MulticastSocket(port);
        socket.joinGroup(new InetSocketAddress(address, PORT_ZERO), LOCAL_NETWORK_INTERFACE_NULL);
//        socket.joinGroup(InetAddress.getByName(address));
        currentUpdater = new Updater();
        Thread t = new Thread(currentUpdater, "LookupService listener");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public synchronized void stop() {
        if (currentUpdater != null) {
            currentUpdater.stop();
            currentUpdater = null;
        }
    }

    @Override
    public RoboContextDescriptor getDescriptor(String id) {
        RoboContextDescriptorEntry entry = entries.get(id);
        return entry != null ? entry.descriptor : localContexts.getDescriptor(id);
    }

    @Override
    public boolean awaitReady(long timeout, TimeUnit unit) throws InterruptedException {
        Updater updater = currentUpdater;
        if (updater == null) {
            throw new IllegalStateException("LookupService has not been started");
        }
        return updater.awaitReady(timeout, unit);
    }
}
