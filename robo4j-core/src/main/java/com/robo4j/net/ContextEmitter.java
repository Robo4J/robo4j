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
import com.robo4j.configuration.Configuration;
import com.robo4j.logging.SimpleLoggingUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * This class is used by the {@link RoboContext} to make it discoverable. This
 * can be configured in the settings for the {@link RoboContext}.
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ContextEmitter {

	/**
	 * Defaults to {@link #DEFAULT_HEARTBEAT_INTERVAL}
	 */
	public static final String KEY_HEARTBEAT_INTERVAL = "heartBeatInterval";

	/**
	 * Defaults to {@link LookupServiceProvider#DEFAULT_MULTICAST_ADDRESS}
	 */
	public static final String KEY_MULTICAST_ADDRESS = "multicastAddress";

	/**
	 * Defaults to {@link LookupServiceProvider#DEFAULT_PORT}
	 */
	public static final String KEY_PORT = "port";

	/**
	 * Defaults to false.
	 */
	public static final String KEY_ENABLED = "enabled";

	/**
	 * The default heartbeat interval.
	 */
	public static final Integer DEFAULT_HEARTBEAT_INTERVAL = 1000;

	private final InetAddress multicastAddress;
	private final int port;
	private final int heartBeatInterval;
	private final DatagramSocket socket;
	private final byte[] message;

	/**
	 * Constructor.
	 * 
	 * @param entry
	 *            the information to emit.
	 * @param multicastAddress
	 *            the address to emit to.
	 * @param port
	 *            the port.
	 * @param heartBeatInterval
	 *            the heart beat interval
	 * @throws SocketException
	 *             possible exception
	 */
	public ContextEmitter(RoboContextDescriptor entry, InetAddress multicastAddress, int port, int heartBeatInterval)
			throws SocketException {
		this.multicastAddress = multicastAddress;
		this.port = port;
		this.heartBeatInterval = heartBeatInterval;
		socket = new DatagramSocket();
		message = HearbeatMessageCodec.encode(entry);
	}

	public ContextEmitter(RoboContextDescriptor entry, Configuration emitterConfiguration)
			throws SocketException, UnknownHostException {
		this(entry,
				InetAddress.getByName(emitterConfiguration.getString(KEY_MULTICAST_ADDRESS,
						LookupServiceProvider.DEFAULT_MULTICAST_ADDRESS)),
				emitterConfiguration.getInteger(KEY_PORT, LookupServiceProvider.DEFAULT_PORT),
				emitterConfiguration.getInteger(KEY_HEARTBEAT_INTERVAL, DEFAULT_HEARTBEAT_INTERVAL));
	}

	/**
	 * Emits a context heartbeat message. Will log any problems.
	 */
	public void emit() {
		try {
			emitWithException();
		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(), "Failed to emit heartbeat message", e);
		}
	}

	/**
	 * Emits a context heartbeat message. Will throw an exception on trouble.
	 * 
	 * @throws IOException
	 *             exception
	 */
	public void emitWithException() throws IOException {
		DatagramPacket packet = new DatagramPacket(message, message.length, multicastAddress, port);
		socket.send(packet);
	}

	public int getHeartBeatInterval() {
		return heartBeatInterval;
	}
}
