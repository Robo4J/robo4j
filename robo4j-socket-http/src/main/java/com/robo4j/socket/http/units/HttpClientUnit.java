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
import com.robo4j.CriticalSectionTrait;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.ProtocolType;
import com.robo4j.socket.http.channel.OutboundHttpSocketChannelHandler;
import com.robo4j.socket.http.enums.StatusCode;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.message.HttpDecoratedResponse;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_PROTOCOL;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_HOST;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_SOCKET_PORT;

/**
 * Http NIO Client for communication with external Robo4J units. Unit accepts
 * @see HttpDecoratedRequest type of message. Such message contains all
 * necessary information and HttpClientDecorator unit is only implementation
 * detail.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@CriticalSectionTrait
public class HttpClientUnit extends RoboUnit<HttpDecoratedRequest> {

	private static final EnumSet<StatusCode> PROCESS_RESPONSES_STATUSES = EnumSet.of(StatusCode.OK,
			StatusCode.ACCEPTED);
	private Integer bufferCapacity;
	private ProtocolType protocol;
	private String host;
	private Integer port;

	public HttpClientUnit(RoboContext context, String id) {
		super(HttpDecoratedRequest.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		bufferCapacity = configuration.getInteger(PROPERTY_BUFFER_CAPACITY, null);
		protocol = ProtocolType.valueOf(configuration.getString(HTTP_PROPERTY_PROTOCOL, "HTTP"));
		host = configuration.getString(PROPERTY_HOST, null);
		port = configuration.getInteger(PROPERTY_SOCKET_PORT, null);
		Objects.requireNonNull(host, "host required");
		if (port == null) {
			port = protocol.getPort();
		}
	}

	@Override
	public void onMessage(HttpDecoratedRequest message) {
		final HttpDecoratedRequest request = adjustRequest(message);
		final InetSocketAddress address = new InetSocketAddress(request.getHost(), request.getPort());
		try (SocketChannel channel = SocketChannel.open(address)) {
			if (bufferCapacity != null) {
				channel.socket().setSendBufferSize(bufferCapacity);
			}

			final HttpDecoratedResponse decoratedResponse = processRequestByChannel(channel, request);

			if (decoratedResponse != null && PROCESS_RESPONSES_STATUSES.contains(decoratedResponse.getCode())) {
				if (!decoratedResponse.getCallbacks().isEmpty()) {
					sendMessageToCallbacks(decoratedResponse.getCallbacks(), decoratedResponse.getMessage());
				}
			} else {
				SimpleLoggingUtil.error(getClass(),
						String.format("no callback or wrong response: %s", decoratedResponse));
			}

		} catch (Exception e) {
			SimpleLoggingUtil.error(getClass(),
					String.format("not available: %s, no worry I continue sending. Error: %s", address, e));
		}
	}

	private HttpDecoratedResponse processRequestByChannel(SocketChannel byteChannel,
														  HttpDecoratedRequest message){
		try (OutboundHttpSocketChannelHandler handler = new OutboundHttpSocketChannelHandler(byteChannel, message)){
			handler.start();
			return handler.getDecoratedResponse();
		}
	}

	private HttpDecoratedRequest adjustRequest(HttpDecoratedRequest request) {
		request.setHost(host);
		request.setPort(port);
		request.addHostHeader();
		return request;
	}

	private void sendMessageToCallbacks(List<String> callbacks, Object message) {
		callbacks.forEach(callback -> sendMessageToCallback(callback, message));
	}

	private void sendMessageToCallback(String callback, Object message) {
		getContext().getReference(callback).sendMessage(message);
	}

}
