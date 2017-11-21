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
import com.robo4j.socket.http.dto.PathMethodDTO;
import com.robo4j.socket.http.enums.StatusCode;
import com.robo4j.socket.http.message.HttpRequestDescriptor;
import com.robo4j.socket.http.message.HttpResponseDescriptor;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.RoboHttpUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_PORT;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_RESPONSE_HANLDER;

/**
 * Http NIO Client for communication with external Robo4J units
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpClientDescriptorUnit extends RoboUnit<HttpRequestDescriptor> {

	private static final EnumSet<StatusCode> PROCESS_RESPONSES_STATUSES = EnumSet.of(StatusCode.OK,
			StatusCode.ACCEPTED);
	private Lock lock = new ReentrantLock();
	private InetSocketAddress address;
	private Integer bufferCapacity;
	private String responseHandler;
	private List<PathMethodDTO> targetPathMethodList;

	public HttpClientDescriptorUnit(RoboContext context, String id) {
		super(HttpRequestDescriptor.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		String confAddress = configuration.getString("address", null);
		int confPort = configuration.getInteger(HTTP_PROPERTY_PORT, RoboHttpUtils.DEFAULT_PORT);
		bufferCapacity = configuration.getInteger(HTTP_PROPERTY_BUFFER_CAPACITY, null);
		responseHandler = configuration.getString(HTTP_PROPERTY_RESPONSE_HANLDER, null);
		targetPathMethodList = JsonUtil.convertJsonToPathMethodList(configuration.getString("targetUnits", null));
		address = new InetSocketAddress(confAddress, confPort);
	}

	@Override
	public void onMessage(HttpRequestDescriptor message) {
		try (SocketChannel channel = SocketChannel.open(address)) {
			if (bufferCapacity != null) {
				channel.socket().setSendBufferSize(bufferCapacity);
			}
			final OutboundChannelHandler handler = new OutboundChannelHandler(targetPathMethodList, channel, message);
			lock.lock();
			try {
				handler.start();
				if (channel.isConnectionPending() && channel.finishConnect()) {
					handler.stop();
				}
			} finally {
				lock.unlock();
			}

			final HttpResponseDescriptor descriptor = handler.getResponseDescriptor();
			if (responseHandler != null) {
				sendMessageToResponseUnit(responseHandler, descriptor);
			}


			if(descriptor != null && PROCESS_RESPONSES_STATUSES.contains(descriptor.getCode())){
				if ( descriptor.getCallbackUnit() != null) {
					sendMessageToResponseUnit(descriptor.getCallbackUnit(), descriptor.getMessage());
				}
			} else {
				SimpleLoggingUtil.error(getClass(), String.format("no callback or wrong response: %s", descriptor));
			}

		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(),
					String.format("not available: %s, no worry I continue sending. Error: %s", address, e));
		}
	}

	private void sendMessageToResponseUnit(String responseUnit, Object message) {
		getContext().getReference(responseUnit).sendMessage(message);
	}

}
