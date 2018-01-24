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

package com.robo4j.socket.http.units;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.channel.OutboundChannelHandler;
import com.robo4j.socket.http.enums.StatusCode;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.message.HttpDecoratedResponse;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.EnumSet;
import java.util.List;

import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_BUFFER_CAPACITY;

/**
 * Http NIO Client for communication with external Robo4J units.
 * Units act similar to {@link HttpClientUnit} and accept {@link HttpDecoratedRequest} type of message.
 * Such message contains all necessary information and HttpClientDecorator unit is only implementation detail.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpClientDecoratorUnit extends RoboUnit<HttpDecoratedRequest> {

	private static final EnumSet<StatusCode> PROCESS_RESPONSES_STATUSES = EnumSet.of(StatusCode.OK,
			StatusCode.ACCEPTED);
	private Integer bufferCapacity;

	public HttpClientDecoratorUnit(RoboContext context, String id) {
		super(HttpDecoratedRequest.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		bufferCapacity = configuration.getInteger(HTTP_PROPERTY_BUFFER_CAPACITY, null);
	}

	// TODO: 12/11/17 (miro) all information are in the message
	@Override
	public void onMessage(HttpDecoratedRequest message) {

		final InetSocketAddress address = new InetSocketAddress(message.getHost(), message.getPort());
		try (SocketChannel channel = SocketChannel.open(address)) {
			if (bufferCapacity != null) {
				channel.socket().setSendBufferSize(bufferCapacity);
			}
			final OutboundChannelHandler handler = new OutboundChannelHandler(channel, message);

			// TODO: 12/10/17 (miro) -> handler
			handler.start();
			handler.stop();

			final HttpDecoratedResponse decoratedResponse = handler.getDecoratedResponse();

			if (decoratedResponse != null && PROCESS_RESPONSES_STATUSES.contains(decoratedResponse.getCode())) {
				if (!decoratedResponse.getCallbacks().isEmpty()) {
					sendMessageToCallbacks(decoratedResponse.getCallbacks(), decoratedResponse.getMessage());
				}
			} else {
				SimpleLoggingUtil.error(getClass(), String.format("no callback or wrong response: %s", decoratedResponse));
			}

		} catch (Exception e) {
			SimpleLoggingUtil.error(getClass(),
					String.format("not available: %s, no worry I continue sending. Error: %s", address, e));
		}
	}

	private void sendMessageToCallbacks(List<String> callbacks, Object message) {
		callbacks.forEach(callback -> sendMessageToCallback(callback, message));
	}

	private void sendMessageToCallback(String callback, Object message) {
		getContext().getReference(callback).sendMessage(message);
	}

}
