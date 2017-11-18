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
import com.robo4j.socket.http.HttpMessageDescriptor;
import com.robo4j.socket.http.channel.OutboundChannelHandler;
import com.robo4j.socket.http.dto.PathMethodDTO;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.RoboHttpUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_PORT;

/**
 * Http NIO Client for communication with external Robo4J units
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpClientUnit2 extends RoboUnit<HttpMessageDescriptor> {

	private InetSocketAddress address;
	private String responseUnit;
	private Integer responseSize;
	private Integer bufferCapacity;
	private List<PathMethodDTO> targetUnitByMethodMap;

	public HttpClientUnit2(RoboContext context, String id) {
		super(HttpMessageDescriptor.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		String confAddress = configuration.getString("address", null);
		int confPort = configuration.getInteger(HTTP_PROPERTY_PORT, RoboHttpUtils.DEFAULT_PORT);
		responseUnit = configuration.getString("responseUnit", null);
		responseSize = configuration.getInteger("responseSize", null);
		bufferCapacity = configuration.getInteger(HTTP_PROPERTY_BUFFER_CAPACITY, null);
		targetUnitByMethodMap = JsonUtil.convertJsonToPathMethodList(configuration.getString("targetUnits", null));

		address = new InetSocketAddress(confAddress, confPort);
	}

	@Override
	public void onMessage(HttpMessageDescriptor message) {
		try {
			SocketChannel channel = SocketChannel.open(address);
			if (bufferCapacity != null) {
				channel.socket().setSendBufferSize(bufferCapacity);
			}
			OutboundChannelHandler handler = new OutboundChannelHandler(targetUnitByMethodMap, channel, message);
			handler.start();
			if (channel.isConnectionPending() && channel.finishConnect()) {
				handler.stop();
			}


		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(),
					String.format("not available: %s, no worry I continue sending. Error: %s", address, e));
		}
	}

	// Private Methods
	private void sendMessageToResponseUnit(ByteBuffer byteBuffer) {
		getContext().getReference(responseUnit).sendMessage(byteBuffer.array());
	}

}
