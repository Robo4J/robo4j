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

import com.robo4j.RoboUnit;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.HttpMessageDescriptor;
import com.robo4j.socket.http.request.RoboRequestCallable;
import com.robo4j.socket.http.request.RoboRequestFactory;
import com.robo4j.socket.http.request.RoboResponseProcess;
import com.robo4j.socket.http.units.HttpCodecRegistry;
import com.robo4j.socket.http.units.HttpUriRegister;
import com.robo4j.socket.http.util.ChannelBufferUtils;
import com.robo4j.socket.http.util.ChannelUtil;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ReadSelectorHandler implements SelectorHendler {

	private final RoboUnit<?> roboUnit;
	private final HttpCodecRegistry codecRegistry;
	private final Map<SelectionKey, RoboResponseProcess> outBuffers;
	private final SelectionKey key;

	public ReadSelectorHandler(RoboUnit<?> roboUnit, HttpCodecRegistry codecRegistry,
			Map<SelectionKey, RoboResponseProcess> outBuffers, SelectionKey key) {
		this.roboUnit = roboUnit;
		this.codecRegistry = codecRegistry;
		this.outBuffers = outBuffers;
		this.key = key;
	}

	@Override
	public SelectionKey handle() {
		try {
			SocketChannel channel = (SocketChannel) key.channel();
			// channel.socket().setKeepAlive(keepAlive);
			long startTime = System.currentTimeMillis();
			final HttpMessageDescriptor messageDescriptor = ChannelBufferUtils
					.getHttpMessageDescriptorByChannel(channel);
			ChannelUtil.printMeasuredTime(getClass(), " bufferWrapper: ", startTime);

			startTime = System.currentTimeMillis();
			HttpUriRegister.getInstance().updateUnits(roboUnit.getContext());
			final RoboRequestFactory factory = new RoboRequestFactory(codecRegistry);
			ChannelUtil.printMeasuredTime(getClass(), " registryUpdate: ", startTime);

			startTime = System.currentTimeMillis();
			final RoboRequestCallable callable = new RoboRequestCallable(roboUnit, messageDescriptor, factory);

			final Future<RoboResponseProcess> futureResult = roboUnit.getContext().getScheduler().submit(callable);
			try {
				RoboResponseProcess result = futureResult.get();
				ChannelUtil.printMeasuredTime(getClass(), " callable message process: ", startTime);
				outBuffers.put(key, result);
			} catch (InterruptedException | ExecutionException e) {
				SimpleLoggingUtil.error(getClass(), "read" + e);
			}

			channel.register(key.selector(), SelectionKey.OP_WRITE);
		} catch (Exception e) {
			SimpleLoggingUtil.error(getClass(), "handle connect", e);
		}
		return key;
	}
}
