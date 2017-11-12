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

import com.robo4j.LifecycleState;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.PropertiesProvider;
import com.robo4j.socket.http.request.RoboResponseProcess;
import com.robo4j.socket.http.units.HttpCodecRegistry;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.robo4j.socket.http.units.HttpServerUnit.PROPERTY_BUFFER_CAPACITY;
import static com.robo4j.socket.http.units.HttpServerUnit.PROPERTY_CODEC_REGISTRY;

/**
 * Inbound context co
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class InboundSocketHandler implements SocketHandler {

	private static final Set<LifecycleState> activeStates = EnumSet.of(LifecycleState.STARTED, LifecycleState.STARTING);
	private final RoboUnit<?> roboUnit;
	private final List<RoboReference<Object>> targetRefs;
	private final Map<SelectionKey, RoboResponseProcess> outBuffers = new ConcurrentHashMap<>();
	private ServerSocketChannel socketChannel;
	private PropertiesProvider properties;
	private boolean active;

	public InboundSocketHandler(RoboUnit<?> roboUnit, List<RoboReference<Object>> targetRefs,
			PropertiesProvider properties) {
		this.roboUnit = roboUnit;
		this.targetRefs = targetRefs;
		this.properties = properties;
	}

	@Override
	public void start() {
		if (!active) {
			active = true;
			roboUnit.getContext().getScheduler().execute(() -> initSocketChannel(targetRefs, properties));
		}
	}

	@Override
	public void stop() {
		try {
			if (socketChannel != null && socketChannel.isOpen()) {
				active = false;
				socketChannel.close();
			}
		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(), "server stop problem: ", e);
		}
	}

	private void initSocketChannel(List<RoboReference<Object>> targetRefs, PropertiesProvider properties) {
		try {

			final Selector selector = Selector.open();

			socketChannel = ServerSocketChannel.open();
			socketChannel.configureBlocking(false);
			socketChannel.bind(new InetSocketAddress(properties.getIntSafe("port")));

			SelectionKey key = socketChannel.register(selector, SelectionKey.OP_ACCEPT);

			while (active) {

				int channelReady = selector.select();
				if (channelReady == 0) {
					continue;
				}

				/*
				 * token representing the registration of a SelectableChannel with a Selector
				 */
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> selectedIterator = selectedKeys.iterator();

				while (selectedIterator.hasNext()) {
					final SelectionKey selectedKey = selectedIterator.next();

					selectedIterator.remove();

					if (selectedKey.isAcceptable()) {
						roboUnit.getContext().getScheduler().submit(() -> new AcceptSelectorHandler(selectedKey,
								properties.getIntSafe(PROPERTY_BUFFER_CAPACITY)));
					} else if (selectedKey.isConnectable()) {
						roboUnit.getContext().getScheduler().submit(() -> new AcceptSelectorHandler(selectedKey,
								properties.getIntSafe(PROPERTY_BUFFER_CAPACITY)));
					} else if (selectedKey.isReadable()) {
						final HttpCodecRegistry codecRegistry = properties
								.getPropertyByClassSafe(PROPERTY_CODEC_REGISTRY);
						roboUnit.getContext().getScheduler().submit(
								() -> new ReadSelectorHandler(roboUnit, codecRegistry, outBuffers, selectedKey));
					} else if (selectedKey.isWritable()) {
						roboUnit.getContext().getScheduler()
								.submit(() -> new WriteSelectorHandler(roboUnit, targetRefs, outBuffers, selectedKey));
					}
				}
			}
		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(), "SERVER CLOSED", e);
		}
		SimpleLoggingUtil.debug(getClass(), "stopped port: " + properties.getIntSafe("port"));
	}
}
