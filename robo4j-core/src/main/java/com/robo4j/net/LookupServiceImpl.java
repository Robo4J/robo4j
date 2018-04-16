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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.net;

import com.robo4j.RoboContext;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.net.LocalLookupServiceImpl.LocalRoboContextDescriptor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

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
	// FIXME(marcus/6 Nov 2017): This should be calculated, and used when
	// constructing the packet
	private final static int MAX_PACKET_SIZE = 1500;
	private MulticastSocket socket;
	private String address;
	private int port;
	private Updater currentUpdater;
	private Map<String, RoboContextDescriptorEntry> entries = new ConcurrentHashMap<>();
	private final LocalLookupServiceImpl localContexts;

	private class Updater implements Runnable {
		private byte[] buffer = new byte[MAX_PACKET_SIZE];
		private volatile boolean isRunning;

		@Override
		public void run() {
			while (!isRunning) {
				try {
					DatagramPacket packet = new DatagramPacket(buffer, MAX_PACKET_SIZE);
					socket.receive(packet);
					process(packet);
				} catch (IOException e) {
					SimpleLoggingUtil.error(getClass(),
							"Failed to look for lookupservice packets. Lookup service will no longer discover new remote contexts.", e);
					isRunning = false;
				}
			}
		}

		private void process(DatagramPacket packet) {
			// First a few quick checks. We want to reject updating anything as
			// early as possible
			if (!HearbeatMessageCodec.isHeartBeatMessage(packet.getData())) {
				SimpleLoggingUtil.debug(getClass(), "Non-heartbeat packet sent to LookupService! Ignoring.");
				return;
			}
			if (!HearbeatMessageCodec.isSupportedVersion(packet.getData())) {
				SimpleLoggingUtil.debug(getClass(), "Wrong protocol heartbeat packet sent to LookupService! Ignoring.");
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
			if (address != null && address instanceof InetSocketAddress) {
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
		}
	}

	public LookupServiceImpl(String address, int port, float missedHeartbeatsBeforeRemoval, LocalLookupServiceImpl localContexts)
			throws SocketException, UnknownHostException {
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
		socket.joinGroup(InetAddress.getByName(address));
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
}
