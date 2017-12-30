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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * This class is used by the {@link RoboContext} to make it discoverable. This
 * can be configured in the settings for the {@link RoboContext}.
 * 
 * @author Marcus Hirt (@hirt)
 */
public final class ContextEmitter {
	private DatagramSocket socket;
	private byte[] message;
	private InetAddress multicastAddress;
	private int port;

	/**
	 * Constructor.
	 * 
	 * @param entry
	 *            the information to emit.
	 * @param multicastAddress
	 *            the address to emit to.
	 * @param port
	 *            the port.
	 * @throws SocketException
	 *             possible exception
	 */
	public ContextEmitter(RoboContextDescriptor entry, InetAddress multicastAddress, int port) throws SocketException {
		this.multicastAddress = multicastAddress;
		this.port = port;
		socket = new DatagramSocket();
		message = HearbeatMessageCodec.encode(entry);
	}

	public void emit() throws IOException {
		DatagramPacket packet = new DatagramPacket(message, message.length, multicastAddress, port);
		socket.send(packet);
	}
}
