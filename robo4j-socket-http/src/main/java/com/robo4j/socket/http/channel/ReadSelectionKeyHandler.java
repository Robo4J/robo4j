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

import com.robo4j.RoboContext;
import com.robo4j.socket.http.SocketException;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.request.HttpResponseProcess;
import com.robo4j.socket.http.request.RoboRequestCallable;
import com.robo4j.socket.http.request.RoboRequestFactory;
import com.robo4j.socket.http.units.CodecRegistry;
import com.robo4j.socket.http.units.ServerContext;
import com.robo4j.socket.http.util.ChannelBufferUtils;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Reading TPC/IP Socket protocol handler
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ReadSelectionKeyHandler implements SelectionKeyHandler {

	private final RoboContext context;
	private final ServerContext serverContext;
	@Deprecated  // FIXME: 2/18/18 codecRegistry are in context
	private final CodecRegistry codecRegistry;
	private final Map<SelectionKey, HttpResponseProcess> outBuffers;
	private final SelectionKey key;

	public ReadSelectionKeyHandler(RoboContext context, ServerContext serverContext, CodecRegistry codecRegistry,
								   Map<SelectionKey, HttpResponseProcess> outBuffers, SelectionKey key) {
		this.context = context;
		this.serverContext = serverContext;
		this.codecRegistry = codecRegistry;
		this.outBuffers = outBuffers;
		this.key = key;
	}

	@Override
	public SelectionKey handle() {
		SocketChannel channel = (SocketChannel) key.channel();
		final HttpDecoratedRequest decoratedRequest = ChannelBufferUtils.getHttpDecoratedRequestByChannel(channel);
		final RoboRequestFactory factory = new RoboRequestFactory(codecRegistry);
		final RoboRequestCallable callable = new RoboRequestCallable(context, serverContext, decoratedRequest, factory);
		final Future<HttpResponseProcess> futureResult = context.getScheduler().submit(callable);
		final HttpResponseProcess result = extractRoboResponseProcess(futureResult);
		outBuffers.put(key, result);
		registerSelectionKey(channel);
		return key;
	}

	private HttpResponseProcess extractRoboResponseProcess(Future<HttpResponseProcess> future) {
		try {
			return future.get();
		} catch (Exception e) {
			throw new SocketException("extract robo response", e);
		}
	}

	private void registerSelectionKey(SocketChannel channel) {
		try {
			channel.register(key.selector(), SelectionKey.OP_WRITE);
		} catch (Exception e) {
			throw new SocketException("register selection key", e);
		}
	}
}
