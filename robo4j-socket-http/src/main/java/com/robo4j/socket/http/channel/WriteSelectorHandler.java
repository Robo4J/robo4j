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
import com.robo4j.socket.http.enums.StatusCode;
import com.robo4j.socket.http.request.RoboResponseProcess;
import com.robo4j.socket.http.SocketException;
import com.robo4j.socket.http.util.ChannelBufferUtils;
import com.robo4j.socket.http.util.ChannelUtil;
import com.robo4j.socket.http.util.HttpHeaderBuilder;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.socket.http.util.RoboResponseHeader;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class WriteSelectorHandler implements SelectorHandler {

	private final RoboContext context;
	private final List<RoboReference<Object>> targetRefs;
	private final Map<SelectionKey, RoboResponseProcess> outBuffers;
	private final SelectionKey key;

	public WriteSelectorHandler(RoboContext context, List<RoboReference<Object>> targetRefs,
			Map<SelectionKey, RoboResponseProcess> outBuffers, SelectionKey key) {
		this.context = context;
		this.targetRefs = targetRefs;
		this.outBuffers = outBuffers;
		this.key = key;
	}

	@Override
	public SelectionKey handle() {
		SocketChannel channel = (SocketChannel) key.channel();

		final RoboResponseProcess responseProcess = outBuffers.get(key);

		ByteBuffer buffer;
		if (responseProcess.getMethod() != null) {
			switch (responseProcess.getMethod()) {
			case GET:
				String getResponse;
				if (responseProcess.getResult() != null && responseProcess.getCode().equals(StatusCode.OK)) {
					String getHeader =  HttpHeaderBuilder.Build()
							.addFirstLine(HttpVersion.HTTP_1_1.getValue())
							.addFirstLine(responseProcess.getCode().getCode())
							.addFirstLine(responseProcess.getCode().getReasonPhrase())
							.add(HttpHeaderFieldNames.ROBO_UNIT_UID, context.getId())
							.build();
					getResponse = RoboHttpUtils.createResponseWithHeaderAndMessage(getHeader,
							responseProcess.getResult().toString());
				} else {
					getResponse = RoboHttpUtils.createResponseByCode(responseProcess.getCode());
				}
				buffer = ChannelBufferUtils.getByteBufferByString(getResponse);
				handleWriteChannelAndBuffer("get write", channel, buffer);
				break;
			case POST:
				if (responseProcess.getResult() != null && responseProcess.getCode().equals(StatusCode.ACCEPTED)) {
					String postResponse = RoboHttpUtils.createResponseByCode(responseProcess.getCode());

					buffer = ChannelBufferUtils.getByteBufferByString(postResponse);
					handleWriteChannelAndBuffer("post write", channel, buffer);
					sendMessageToTargetRoboReference(targetRefs, responseProcess);
				} else {
					String notImplementedResponse = RoboHttpUtils.createResponseByCode(responseProcess.getCode());
					buffer = ChannelBufferUtils.getByteBufferByString(notImplementedResponse);
					handleWriteChannelAndBuffer("post write", channel, buffer);
				}
			default:
				break;
			}
		} else {
			String badResponse = RoboResponseHeader.headerByCode(StatusCode.BAD_REQUEST);
			buffer = ChannelBufferUtils.getByteBufferByString(badResponse);
			try {
				ChannelUtil.writeBuffer(channel, buffer);
			} catch (Exception e) {
				throw new SocketException("post write", e);
			}
			buffer.clear();
		}

		// channelKeyMap.remove(channel);

		try {
			channel.close();
		} catch (Exception e) {
			throw new SocketException("handle write channel close", e);
		}
		key.cancel();
		return key;
	}

	private void sendMessageToTargetRoboReference(List<RoboReference<Object>> targetRefs, RoboResponseProcess process) {
		targetRefs.stream().filter(
				ref -> process.getResult() != null && ref.getMessageType().equals(process.getResult().getClass()))
				.forEach(ref -> ref.sendMessage(process.getResult()));
	}

	private void handleWriteChannelAndBuffer(String message, ByteChannel channel, ByteBuffer buffer) {
		try {
			ChannelUtil.writeBuffer(channel, buffer);
		} catch (Exception e) {
			throw new SocketException(message, e);
		} finally {
			buffer.clear();
		}
	}
}
