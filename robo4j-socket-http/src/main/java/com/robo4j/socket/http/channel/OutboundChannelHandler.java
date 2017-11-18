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

import com.robo4j.socket.http.message.HttpRequestDescriptor;
import com.robo4j.socket.http.SocketException;
import com.robo4j.socket.http.dto.PathMethodDTO;
import com.robo4j.socket.http.message.HttpResponseDescriptor;
import com.robo4j.socket.http.util.ChannelBufferUtils;
import com.robo4j.socket.http.util.ChannelUtil;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.List;

/**
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class OutboundChannelHandler implements SocketHandler {

	private ByteChannel byteChannel;
	private List<PathMethodDTO> targetUnitByMethodMap;
	private HttpRequestDescriptor message;
	private HttpResponseDescriptor receivedMessage;

	public OutboundChannelHandler(List<PathMethodDTO> targetUnitByMethodMap, ByteChannel byteChannel,
			HttpRequestDescriptor message) {
		this.targetUnitByMethodMap = targetUnitByMethodMap;
		this.byteChannel = byteChannel;
		this.message = message;
	}

	@Override
	public void start() {
		final PathMethodDTO pathMethod = new PathMethodDTO(message.getPath(), message.getMethod(), null);
		if (targetUnitByMethodMap.contains(pathMethod)) {
			// final ByteBuffer buffer = processMessageToClient(message);
			final ByteBuffer buffer = ChannelBufferUtils.getByteBufferByString(message.getMessage());
			ChannelUtil.handleWriteChannelAndBuffer("client send message", byteChannel, buffer);
			switch (message.getMethod()) {
			case GET:
				try {
					System.out.println(getClass() + " try to read received message");
					receivedMessage = ChannelBufferUtils.getHttpResponseDescriptorByChannel(byteChannel);
				} catch (Exception e) {
					throw new SocketException("message body write problem", e);
				}
				break;
			case HEAD:
			case PUT:
			case POST:
			case PATCH:
				break;
			case TRACE:
			case OPTIONS:
			case DELETE:
			default:
				throw new SocketException(String.format("not implemented method: %s", message));
			}
			buffer.clear();
		}
	}

	@Override
	public void stop() {
		try {
			byteChannel.close();
		} catch (Exception e) {
			throw new SocketException("closing channel problem", e);
		}
	}

	public HttpResponseDescriptor getResponseMessage() {
		return receivedMessage;
	}

}
