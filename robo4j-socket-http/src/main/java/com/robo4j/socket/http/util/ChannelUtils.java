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

package com.robo4j.socket.http.util;

import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.SocketException;
import com.robo4j.socket.http.channel.DatagramConnectionType;
import com.robo4j.socket.http.channel.SelectionKeyHandler;
import com.robo4j.socket.http.units.ClientContext;
import com.robo4j.socket.http.units.ServerContext;
import com.robo4j.socket.http.units.SocketContext;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_HOST;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_SOCKET_PORT;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class ChannelUtils {
	/**
	 * reading buffer
	 *
	 * @param channel
	 *            byte channel
	 * @param buffer
	 *            buffer
	 * @return read bytes
	 * @throws IOException
	 *             exception
	 */
	public static int readBuffer(ByteChannel channel, ByteBuffer buffer) throws IOException {
		int numberRead = channel.read(buffer);
		int position = 0;
		int totalRead = numberRead;
		while (numberRead >= 0 && position <= buffer.capacity()) {
			numberRead = channel.read(buffer);
			if (numberRead > 0) {
				totalRead += numberRead;
			}
			position++;
		}
		return totalRead;
	}

	/**
	 * writing to channel buffer
	 *
	 * @param channel
	 *            byte channel
	 * @param buffer
	 *            buffer
	 * @return written bytes
	 * @throws IOException
	 *             exception
	 */
	public static int writeBuffer(ByteChannel channel, ByteBuffer buffer) throws IOException {
		int numberWritten = 0;
		int totalWritten = numberWritten;

		while (numberWritten >= 0 && buffer.hasRemaining()) {
			numberWritten = channel.write(buffer);
			totalWritten += numberWritten;
		}
		return totalWritten;
	}

	public static SelectionKey registerSelectionKey(AbstractSelectableChannel result) {
		try {
			final Selector selector = Selector.open();
			return result.register(selector, SelectionKey.OP_ACCEPT);
		} catch (Exception e) {
			SimpleLoggingUtil.error(ChannelUtils.class, "resister selection key", e);
			throw new SocketException("resister selection key", e);
		}
	}

	public static SelectionKey registerDatagramSelectionKey(AbstractSelectableChannel result) {
		try {
			final Selector selector = Selector.open();
			return result.register(selector, SelectionKey.OP_READ, SelectionKey.OP_WRITE);
		} catch (Exception e) {
			SimpleLoggingUtil.error(ChannelUtils.class, "resister selection key", e);
			throw new SocketException("resister selection key", e);
		}
	}

	public static ServerSocketChannel initServerSocketChannel(ServerContext context) {
		try {
			final ServerSocketChannel result = ServerSocketChannel.open();
			result.bind(new InetSocketAddress(context.getPropertySafe(Integer.class, PROPERTY_SOCKET_PORT)));
			result.configureBlocking(false);
			return result;
		} catch (Exception e) {
			SimpleLoggingUtil.error(ChannelUtils.class, "init server socket channel", e);
			throw new SocketException("init server socket channel", e);
		}
	}

	public static DatagramChannel initDatagramChannel(DatagramConnectionType connectionType, SocketContext<?> context){
		try {
			final DatagramChannel result = DatagramChannel.open();
			SocketAddress address = getSocketAddressByContext(context);
			switch (connectionType){
				case SERVER:
					DatagramSocket socket = result.socket();
					result.configureBlocking(false);
					socket.bind(address);
					break;
				case CLIENT:
					result.connect(address);
					break;
				default:
					throw new SocketException("int not supported: " + connectionType);
			}
			return result;

		} catch (Exception e){
			SimpleLoggingUtil.error(ChannelUtils.class, "init datagram socket channel", e);
			throw new SocketException("init datagram socket channel", e);
		}
	}

	public static SocketAddress getSocketAddressByContext(SocketContext<?> context){
		if(context instanceof ClientContext){
			final String clientHost = context.getPropertySafe(String.class, PROPERTY_HOST);
			final int clientPort = context.getPropertySafe(Integer.class, PROPERTY_SOCKET_PORT);
			return new InetSocketAddress(clientHost,clientPort);
		} else if (context instanceof ServerContext){
			final int serverPort = context.getPropertySafe(Integer.class, PROPERTY_SOCKET_PORT);
			return new InetSocketAddress(serverPort);
		} else {
			throw new SocketException("invalid context" + context);
		}
	}

	public static int getReadyChannelBySelectionKey(SelectionKey key) {
		return getReadyChannelBySelectionKey(key, 0L);
	}

	public static int getReadyChannelBySelectionKey(SelectionKey key, long timeout) {
		try {
			return key.selector().select(timeout);
		} catch (Exception e) {
			SimpleLoggingUtil.error(ChannelUtils.class, "get ready channel by selection key", e);
			throw new SocketException("get ready channel by selection key", e);
		}
	}

	public static void handleWriteChannelAndBuffer(String message, ByteChannel channel, ByteBuffer buffer) {
		try {
			ChannelUtils.writeBuffer(channel, buffer);
		} catch (Exception e) {
			throw new SocketException(message, e);
		} finally {
			buffer.clear();
		}
	}

	public static void handleSelectorHandler(SelectionKeyHandler handler) {
		handler.handle();
	}

}
