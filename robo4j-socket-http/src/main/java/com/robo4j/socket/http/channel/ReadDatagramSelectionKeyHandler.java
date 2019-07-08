/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.channel;

import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.socket.http.SocketException;
import com.robo4j.socket.http.request.DatagramResponseProcess;
import com.robo4j.socket.http.units.CodecRegistry;
import com.robo4j.socket.http.units.PathHttpMethod;
import com.robo4j.socket.http.units.ServerContext;
import com.robo4j.socket.http.units.ServerPathConfig;
import com.robo4j.socket.http.units.SocketDecoder;
import com.robo4j.socket.http.util.ChannelBufferUtils;
import com.robo4j.socket.http.util.HttpConstant;
import com.robo4j.socket.http.util.RoboHttpUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.Map;

import static com.robo4j.socket.http.util.HttpMessageUtils.HTTP_HEADER_BODY_DELIMITER;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_BYTE_BUFFER;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_REGISTRY;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ReadDatagramSelectionKeyHandler implements SelectionKeyHandler {
	private final ServerContext serverContext;
	private final Map<SelectionKey, DatagramResponseProcess> outBuffers;
	private final SelectionKey key;
	private final CodecRegistry codecRegistry;

	public ReadDatagramSelectionKeyHandler(RoboContext context, ServerContext serverContext,
                                           Map<SelectionKey, DatagramResponseProcess> outBuffers, SelectionKey key) {
		this.serverContext = serverContext;
		this.outBuffers = outBuffers;
		this.key = key;
		codecRegistry = serverContext.getPropertySafe(CodecRegistry.class, PROPERTY_CODEC_REGISTRY);
	}

	// TODO: 2/25/18 (miro) -> Datagram type -> not only string is possible
	@Override
	public SelectionKey handle() {
		final DatagramChannel channel = (DatagramChannel) key.channel();
		try {
			ByteBuffer buffer = serverContext.getPropertySafe(ByteBuffer.class, PROPERTY_BYTE_BUFFER);
			buffer.clear();
			channel.receive(buffer);
			buffer.flip();
			String message = ChannelBufferUtils.byteBufferToString(buffer);

            final String[] headerAndBody = message.split(HTTP_HEADER_BODY_DELIMITER);
            final String firstLine = RoboHttpUtils.correctLine(headerAndBody[0]);
            final String[] tokens = firstLine.split(HttpConstant.HTTP_EMPTY_SEP);
            final String body = headerAndBody[1];
            final ServerPathConfig serverPathConfig = serverContext.getPathConfig(new PathHttpMethod(tokens[1], null));

            final RoboReference<Object> roboReference = serverPathConfig.getRoboUnit();

			final SocketDecoder<Object, Object> decoder = codecRegistry.getDecoder(roboReference.getMessageType());
            final Object decodedMessage = decoder.decode(body);
			serverPathConfig.getRoboUnit().sendMessage(decodedMessage);

			final DatagramResponseProcess responseProcess = new DatagramResponseProcess(tokens[1], roboReference, decodedMessage);

			outBuffers.put(key, responseProcess);
			return key;

		} catch (IOException e) {
			throw new SocketException("hanlde", e);
		}
	}
}
