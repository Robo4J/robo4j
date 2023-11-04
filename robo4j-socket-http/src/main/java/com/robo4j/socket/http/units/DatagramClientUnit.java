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
package com.robo4j.socket.http.units;

import com.robo4j.ConfigurationException;
import com.robo4j.CriticalSectionTrait;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.channel.OutboundDatagramSocketChannelHandler;
import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.socket.http.message.DatagramDecoratedRequest;
import com.robo4j.socket.http.util.CodeRegistryUtils;
import com.robo4j.socket.http.util.DatagramPathUtils;
import com.robo4j.socket.http.util.JsonUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.robo4j.socket.http.util.ChannelBufferUtils.INIT_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_PACKAGES;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_REGISTRY;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_HOST;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_SOCKET_PORT;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_UNIT_PATHS_CONFIG;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@CriticalSectionTrait
public class DatagramClientUnit extends RoboUnit<DatagramDecoratedRequest> {

	private final ClientContext clientContext = new ClientContext();
	private AtomicBoolean active = new AtomicBoolean(false);

	public DatagramClientUnit(RoboContext context, String id) {
		super(DatagramDecoratedRequest.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		String host = configuration.getString(PROPERTY_HOST, null);
		int port = configuration.getInteger(PROPERTY_SOCKET_PORT, null);

		int bufferCapacity = configuration.getInteger(PROPERTY_BUFFER_CAPACITY, INIT_BUFFER_CAPACITY);

		String packages = configuration.getString(PROPERTY_CODEC_PACKAGES, null);

		final List<HttpPathMethodDTO> paths = JsonUtil.readPathConfig(HttpPathMethodDTO.class,
				configuration.getString(PROPERTY_UNIT_PATHS_CONFIG, null));

		// FIXME: 2/25/18 (miro) should be moved
		if (paths.isEmpty()) {
			throw ConfigurationException.createMissingConfigNameException(PROPERTY_UNIT_PATHS_CONFIG);
		}

		DatagramPathUtils.updateDatagramClientContextPaths(clientContext, paths);

		clientContext.putProperty(PROPERTY_HOST, host);
		clientContext.putProperty(PROPERTY_SOCKET_PORT, port);
		clientContext.putProperty(PROPERTY_BUFFER_CAPACITY, bufferCapacity);
		clientContext.putProperty(PROPERTY_CODEC_REGISTRY, CodeRegistryUtils.getCodecRegistry(packages));
	}

	@Override
	public void start() {
		super.start();
		active.set(true);
	}

	@Override
	public void stop() {
		super.stop();
		active.set(false);
	}

	@Override
	public void onMessage(DatagramDecoratedRequest request) {

		// TODO: 2/25/18 (miro) -> continue, handler
		OutboundDatagramSocketChannelHandler handler = new OutboundDatagramSocketChannelHandler(getContext(),
				clientContext, request.toMessage());
		handler.start();
		handler.stop();
	}
}
