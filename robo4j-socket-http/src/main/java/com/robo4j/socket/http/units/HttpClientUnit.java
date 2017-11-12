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

package com.robo4j.socket.http.units;

import com.robo4j.BlockingTrait;
import com.robo4j.ConfigurationException;
import com.robo4j.CriticalSectionTrait;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.util.ChannelUtil;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.RoboHttpUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

/**
 * Http NIO Client to communicate with external system/Robo4J units
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@BlockingTrait
@CriticalSectionTrait
public class HttpClientUnit extends RoboUnit<Object> {
	private static final String PROPERTY_KEEP_ALIVE = "keepAlive";
	private InetSocketAddress address;
	private String responseUnit;
	private Integer responseSize;
	private Integer bufferCapacity;
	private boolean keepAlive;
	private boolean blocking;

	public HttpClientUnit(RoboContext context, String id) {
		super(Object.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		setState(LifecycleState.UNINITIALIZED);
		String confAddress = configuration.getString("address", null);
		int confPort = configuration.getInteger("port", RoboHttpUtils.DEFAULT_PORT);
		responseUnit = configuration.getString("responseUnit", null);
		responseSize = configuration.getInteger("responseSize", null);
		bufferCapacity = configuration.getInteger("bufferCapacity", null);
		blocking = configuration.getBoolean("blocking", false);
		keepAlive = configuration.getBoolean(PROPERTY_KEEP_ALIVE, false);

		Map<String, Object> targetUnitsMap = JsonUtil.getMapNyJson(configuration.getString("targetUnits", null));
		if (confAddress == null || targetUnitsMap.isEmpty()) {
			throw ConfigurationException.createMissingConfigNameException("address, path, commands...");
		}
		address = new InetSocketAddress(confAddress, confPort);

		setState(LifecycleState.INITIALIZED);
	}

	@Override
	public void onMessage(Object message) {
		try {
			SocketChannel channel = SocketChannel.open(address);
			channel.configureBlocking(blocking);
			if (bufferCapacity != null) {
				channel.socket().setSendBufferSize(bufferCapacity);
			}
			channel.socket().setKeepAlive(keepAlive);

			String processMessage = message.toString();

			byte[] bytes = processMessage.getBytes();
			ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
			buffer.put(bytes);
			buffer.flip();

			int writtenBytes = ChannelUtil.writeBuffer(channel, buffer);
			System.out.println(getClass().getSimpleName() + " onMessage, writtenBytes: " + writtenBytes + " bytes: "
					+ bytes.length);

			if (responseUnit != null && responseSize != null) {
				ByteBuffer readBuffer = ByteBuffer.allocate(responseSize);
				// important is set stopper properly
				ChannelUtil.readBuffer(channel, readBuffer);
				sendMessageToResponseUnit(readBuffer);
			}

			buffer.clear();
			channel.close();

		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(), "not available:" + address + ", no worry I continue sending: " + e);
//			SimpleLoggingUtil.error(getClass(), "not available:" + address + ",  stack: "
//					+ Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toList()));
		}
	}

	// Private Methods
	private void sendMessageToResponseUnit(ByteBuffer byteBuffer) {
		getContext().getReference(responseUnit).sendMessage(byteBuffer.array());
	}

}
