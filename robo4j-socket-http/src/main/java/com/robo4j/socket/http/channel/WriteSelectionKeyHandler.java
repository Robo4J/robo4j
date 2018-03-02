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
import com.robo4j.RoboReference;
import com.robo4j.socket.http.HttpHeaderFieldNames;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.SocketException;
import com.robo4j.socket.http.enums.StatusCode;
import com.robo4j.socket.http.message.HttpResponseDenominator;
import com.robo4j.socket.http.request.HttpResponseProcess;
import com.robo4j.socket.http.units.ServerContext;
import com.robo4j.socket.http.units.ServerPathConfig;
import com.robo4j.socket.http.util.ChannelBufferUtils;
import com.robo4j.socket.http.util.ChannelUtils;
import com.robo4j.socket.http.message.HttpDenominator;
import com.robo4j.socket.http.util.HttpMessageBuilder;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class WriteSelectionKeyHandler implements SelectionKeyHandler {

	private final RoboContext context;
	private final ServerContext serverContext;
	private final Map<SelectionKey, HttpResponseProcess> outBuffers;
	private final SelectionKey key;

	public WriteSelectionKeyHandler(RoboContext context, ServerContext serverContext,
                                    Map<SelectionKey, HttpResponseProcess> outBuffers, SelectionKey key) {
		this.context = context;
		this.serverContext = serverContext;
		this.outBuffers = outBuffers;
		this.key = key;
	}

	@Override
	public SelectionKey handle() {
		SocketChannel channel = (SocketChannel) key.channel();

		final HttpResponseProcess responseProcess = outBuffers.get(key);

		ByteBuffer buffer;
		if (responseProcess.getMethod() != null) {
			switch (responseProcess.getMethod()) {
			case GET:
				String getResponse;
				if (responseProcess.getResult() != null && responseProcess.getCode().equals(StatusCode.OK)) {
					// FIXME: 2/18/18 (miro) put abstraction
					String responseMessage = responseProcess.getResult().toString();
					HttpDenominator denominator = new HttpResponseDenominator(responseProcess.getCode(),
							HttpVersion.HTTP_1_1);
					getResponse = HttpMessageBuilder.Build().setDenominator(denominator)
							.addHeaderElement(HttpHeaderFieldNames.ROBO_UNIT_UID, context.getId())
							.addHeaderElement(HttpHeaderFieldNames.CONTENT_LENGTH,
									String.valueOf(responseMessage.length()))
							.build(responseMessage);
				} else {
					HttpDenominator denominator = new HttpResponseDenominator(responseProcess.getCode(),
							HttpVersion.HTTP_1_1);
					getResponse = HttpMessageBuilder.Build().setDenominator(denominator).build();
				}
				buffer = ChannelBufferUtils.getByteBufferByString(getResponse);
				ChannelUtils.handleWriteChannelAndBuffer("get write", channel, buffer);
				break;
			case POST:
				if (responseProcess.getResult() != null && responseProcess.getCode().equals(StatusCode.ACCEPTED)) {

					HttpDenominator denominator = new HttpResponseDenominator(responseProcess.getCode(),
							HttpVersion.HTTP_1_1);
					String postResponse = HttpMessageBuilder.Build().setDenominator(denominator).build();

					buffer = ChannelBufferUtils.getByteBufferByString(postResponse);
					ChannelUtils.handleWriteChannelAndBuffer("post write", channel, buffer);
					sendMessageToTargetRoboReference(responseProcess);
				} else {
					HttpDenominator denominator = new HttpResponseDenominator(responseProcess.getCode(),
							HttpVersion.HTTP_1_1);
					String notImplementedResponse = HttpMessageBuilder.Build().setDenominator(denominator).build();
					buffer = ChannelBufferUtils.getByteBufferByString(notImplementedResponse);
					ChannelUtils.handleWriteChannelAndBuffer("post write", channel, buffer);
				}
			default:
				break;
			}
		} else {
			HttpDenominator denominator = new HttpResponseDenominator(StatusCode.BAD_REQUEST, HttpVersion.HTTP_1_1);
			String badResponse = HttpMessageBuilder.Build().setDenominator(denominator).build();
			buffer = ChannelBufferUtils.getByteBufferByString(badResponse);
			try {
				ChannelUtils.writeBuffer(channel, buffer);
			} catch (Exception e) {
				throw new SocketException("post write", e);
			}
			buffer.clear();
		}

//		 channelKeyMap.remove(channel);

		key.cancel();
//		try {
//			channel.close();
//		} catch (Exception e) {
//			throw new SocketException("handle write channel close", e);
//		}
		return key;
	}

	private void sendMessageToTargetRoboReference(HttpResponseProcess process) {
		final ServerPathConfig pathConfig = serverContext.getPathConfig(process.getPath());
		if (pathConfig.getRoboUnit() != null
				&& pathConfig.getRoboUnit().getMessageType().equals(process.getResult().getClass())) {
			RoboReference<Object> reference = pathConfig.getRoboUnit();
			reference.sendMessage(process.getResult());
		} else {
			throw new IllegalStateException(String.format("process %s", process));
		}
	}

}
