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

import com.robo4j.socket.http.SocketException;
import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.message.HttpDecoratedResponse;
import com.robo4j.socket.http.util.ChannelBufferUtils;
import com.robo4j.socket.http.util.ChannelResponseBuffer;
import com.robo4j.socket.http.util.ChannelUtils;
import com.robo4j.socket.http.util.HttpMessageBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class OutboundHttpSocketChannelHandler implements ChannelHandler, AutoCloseable {

	private ByteChannel byteChannel;
	private HttpDecoratedRequest message;
	private HttpDecoratedResponse decoratedResponse;
	private ChannelResponseBuffer channelResponseBuffer = new ChannelResponseBuffer();
	private Lock lock = new ReentrantLock();

	public OutboundHttpSocketChannelHandler(ByteChannel byteChannel, HttpDecoratedRequest message) {
		this.byteChannel = byteChannel;
		this.message = message;
	}

	@Override
	public void start() {
		// FIXME: 1/24/18 (miro) -> client context
		final HttpPathMethodDTO pathMethod = new HttpPathMethodDTO(message.getPathMethod().getPath(), message.getPathMethod().getMethod(),
				message.getCallbacks());

		//@formatter:off
		final String resultMessage = HttpMessageBuilder.Build()
				.setDenominator(message.getDenominator())
				.addHeaderElements(message.getHeader())
				.build(message.getMessage());
		//@formatter:on

		final ByteBuffer buffer = ChannelBufferUtils.getByteBufferByString(resultMessage);
		ChannelUtils.handleWriteChannelAndBuffer("client send message", byteChannel, buffer);
		decoratedResponse = getDecoratedResponse(byteChannel, pathMethod);
	}

	@Override
	public void stop() {
		try {
			byteChannel.close();
		} catch (Exception e) {
			throw new SocketException("closing channel problem", e);
		}
	}

	@Override
	public void close() {
		stop();
	}

	public HttpDecoratedResponse getDecoratedResponse() {
		return decoratedResponse;
	}

	private HttpDecoratedResponse getDecoratedResponse(ByteChannel byteChannel, HttpPathMethodDTO pathMethod) {
		lock.lock();
		try {
			final HttpDecoratedResponse result = channelResponseBuffer.getHttpDecoratedResponseByChannel(byteChannel);
			result.addCallbacks(pathMethod.getCallbacks());
			return result;
		} catch (IOException e) {
			throw new SocketException("message body write problem", e);
		} finally {
			lock.unlock();
		}
	}

}
