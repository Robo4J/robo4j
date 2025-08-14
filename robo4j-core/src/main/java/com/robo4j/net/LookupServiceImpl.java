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

import static com.robo4j.net.HearbeatMessageCodec.notHeartBeatMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet6Address;
import java.net.InetAddress;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.robo4j.RoboContext;
import com.robo4j.net.LocalLookupServiceImpl.LocalRoboContextDescriptor;

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

	private final static int DEFAULT_MAX_PACKET_SIZE = 1500;
	private final InetAddress multicastGroup;
	private final NetworkInterface networkInterface;
	private final int port;
	private final Map<String, RoboContextDescriptorEntry> entries = new ConcurrentHashMap<>();
	private MulticastSocket socket;
	private Updater currentUpdater;
	private final LocalLookupServiceImpl localContexts;
	private int maxPacketSize = DEFAULT_MAX_PACKET_SIZE;

	private class Updater implements Runnable {
		private final byte[] buffer;
		private volatile boolean isRunning = true;
		private volatile Thread runnerThread;

		public Updater() {
			this.buffer = new byte[maxPacketSize];
		}

		@Override
		public void run() {
			runnerThread = Thread.currentThread();
			while (isRunning && !Thread.currentThread().isInterrupted()) {
				try {
					DatagramPacket packet = new DatagramPacket(buffer, maxPacketSize);
					socket.receive(packet);
					process(packet);
				} catch (IOException e) {
					if (isRunning) { // Only log if not intentionally stopped
						LOGGER.error("Failed to look for lookupservice packets. Lookup service will no longer discover new remote contexts.",
								e);
					}
					break;
				}
			}
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
			isRunning = false;
			if (runnerThread != null) {
				runnerThread.interrupt();
			}
		}
	}

	public LookupServiceImpl(InetAddress multicastGroup, NetworkInterface networkInterface, int port, float missedHeartbeatsBeforeRemoval, LocalLookupServiceImpl localContexts)
			throws SocketException, UnknownHostException {
		this.multicastGroup = multicastGroup;
		this.networkInterface = networkInterface;
		this.port = port;
		this.localContexts = localContexts;

		// Calculate appropriate packet size based on interface MTU
		calculateMaxPacketSize();
	}

	private void calculateMaxPacketSize() {
		try {
			int mtu = networkInterface.getMTU();
			// Reserve space for IP header (20 bytes IPv4, 40 bytes IPv6) and UDP header (8 bytes)
			boolean isIPv6 = multicastGroup instanceof Inet6Address;
			int headerOverhead = isIPv6 ? 48 : 28;
			maxPacketSize = Math.max(512, mtu - headerOverhead); // Minimum 512 bytes
			LOGGER.debug("Using packet size {} for interface {} (MTU: {})", maxPacketSize, networkInterface.getName(), mtu);
		} catch (SocketException e) {
			LOGGER.warn("Could not determine MTU for interface {}, using default packet size {}", networkInterface.getName(), maxPacketSize);
		}
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
		socket.setReuseAddress(true);
		socket.setNetworkInterface(networkInterface);
		socket.joinGroup(new InetSocketAddress(multicastGroup, port), networkInterface);

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
		if (socket != null) {
			try {
				socket.leaveGroup(new InetSocketAddress(multicastGroup, port), networkInterface);
			} catch (IOException e) {
				LOGGER.warn("Failed to leave multicast group during stop", e);
			}
			socket.close();
			socket = null;
		}
	}

	@Override
	public RoboContextDescriptor getDescriptor(String id) {
		RoboContextDescriptorEntry entry = entries.get(id);
		return entry != null ? entry.descriptor : localContexts.getDescriptor(id);
	}
}
