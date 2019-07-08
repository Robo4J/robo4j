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
package com.robo4j.socket.http.units;

import static com.robo4j.socket.http.util.ChannelBufferUtils.CHANNEL_TIMEOUT;
import static com.robo4j.socket.http.util.ChannelBufferUtils.INIT_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_BYTE_BUFFER;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_PACKAGES;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_REGISTRY;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_SOCKET_PORT;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_TIMEOUT;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_UNIT_PATHS_CONFIG;

import java.nio.ByteBuffer;
import java.util.List;

import com.robo4j.BlockingTrait;
import com.robo4j.ConfigurationException;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.channel.InboundDatagramSocketChannelHandler;
import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.socket.http.util.CodeRegistryUtils;
import com.robo4j.socket.http.util.DatagramPathUtils;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.RoboHttpUtils;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@BlockingTrait
public class DatagramServerUnit extends RoboUnit<Object> {

	private final ServerContext serverContext = new ServerContext();
	private List<HttpPathMethodDTO> paths;
	private InboundDatagramSocketChannelHandler handler;

	public DatagramServerUnit(RoboContext context, String id) {
		super(Object.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		int port = configuration.getInteger(PROPERTY_SOCKET_PORT, RoboHttpUtils.DEFAULT_UDP_PORT);
		int timeout = configuration.getInteger(PROPERTY_TIMEOUT, CHANNEL_TIMEOUT);
		int bufferCapacity = configuration.getInteger(PROPERTY_BUFFER_CAPACITY, INIT_BUFFER_CAPACITY);

		String packages = configuration.getString(PROPERTY_CODEC_PACKAGES, null);
		paths = JsonUtil.readPathConfig(HttpPathMethodDTO.class, configuration.getString(PROPERTY_UNIT_PATHS_CONFIG, null));
		if (paths.isEmpty()) {
			throw ConfigurationException.createMissingConfigNameException(PROPERTY_UNIT_PATHS_CONFIG);
		}

		serverContext.putProperty(PROPERTY_BYTE_BUFFER, ByteBuffer.allocateDirect(bufferCapacity));
		serverContext.putProperty(PROPERTY_SOCKET_PORT, port);
		serverContext.putProperty(PROPERTY_CODEC_REGISTRY, CodeRegistryUtils.getCodecRegistry(packages));
		serverContext.putProperty(PROPERTY_TIMEOUT, timeout);
	}

	/**
	 * start updates context by references
	 */
	@Override
	public void start() {
		setState(LifecycleState.STARTING);
		DatagramPathUtils.updateDatagramServerContextPaths(getContext(), serverContext, paths);
		handler = new InboundDatagramSocketChannelHandler(getContext(), serverContext);
		handler.start();
		setState(LifecycleState.STARTED);
	}

	@Override
	public void stop() {
		setState(LifecycleState.STOPPING);
		handler.stop();
		setState(LifecycleState.STOPPED);
	}

}
