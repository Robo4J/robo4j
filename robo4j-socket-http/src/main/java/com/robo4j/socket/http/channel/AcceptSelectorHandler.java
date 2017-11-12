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

package com.robo4j.socket.http.channel;

import com.robo4j.logging.SimpleLoggingUtil;

import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Handle OP_ACCEPT
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class AcceptSelectorHandler implements SelectorHendler {

	private final SelectionKey key;
	private final int bufferCapacity;

	public AcceptSelectorHandler(SelectionKey key, int bufferCapacity) {
		this.key = key;
		this.bufferCapacity = bufferCapacity;
	}

	@Override
	public SelectionKey handle() {
		try {
			ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
			SocketChannel channel = serverChannel.accept();
			serverChannel.socket().setReceiveBufferSize(bufferCapacity);
			channel.configureBlocking(false);
			channel.register(key.selector(), SelectionKey.OP_READ);
		} catch (Exception e) {
			SimpleLoggingUtil.error(getClass(), "handle accept", e);
		}
		return key;
	}
}
