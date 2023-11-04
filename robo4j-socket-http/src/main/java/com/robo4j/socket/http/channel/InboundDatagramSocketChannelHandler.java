/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
import com.robo4j.socket.http.request.DatagramResponseProcess;
import com.robo4j.socket.http.units.ServerContext;
import com.robo4j.socket.http.util.ChannelUtils;

import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.robo4j.socket.http.util.ChannelUtils.handleSelectorHandler;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_TIMEOUT;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class InboundDatagramSocketChannelHandler implements ChannelHandler {

	private final Map<SelectionKey, DatagramResponseProcess> outBuffers = new ConcurrentHashMap<>();
	private final RoboContext context;
	private final ServerContext serverContext;
	private boolean active;

	public InboundDatagramSocketChannelHandler(RoboContext context, ServerContext serverContext) {
		this.context = context;
		this.serverContext = serverContext;
	}

	@Override
	public void start() {
		if (!active) {
			active = true;
			context.getScheduler().execute(() -> initDatagramChannel(serverContext));
		}
	}

	/**
	 * doesn't need to be stopped
	 */
	@Override
	public void stop() {

	}

	private void initDatagramChannel(ServerContext serverContext) {
		final DatagramChannel channel = ChannelUtils.initDatagramChannel(DatagramConnectionType.SERVER, serverContext);
		final SelectionKey key = ChannelUtils.registerDatagramSelectionKey(channel);

		final int timeout = serverContext.getPropertySafe(Integer.class, PROPERTY_TIMEOUT);
		while (active) {
			ChannelUtils.getReadyChannelBySelectionKey(key, timeout);

			Set<SelectionKey> selectedKeys = key.selector().selectedKeys();
			Iterator<SelectionKey> selectedIterator = selectedKeys.iterator();

			while (selectedIterator.hasNext()) {
				final SelectionKey selectedKey = selectedIterator.next();

				selectedIterator.remove();

				if (selectedKey.isReadable()) {
					handleSelectorHandler(
							new ReadDatagramSelectionKeyHandler(context, serverContext, outBuffers, selectedKey));
				}
				if (selectedKey.isWritable()) {
					handleSelectorHandler(
							new WriteDatagramSelectionKeyHandler(context, serverContext, outBuffers, selectedKey));
				}
			}
		}

	}

}
