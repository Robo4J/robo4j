/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This HttpDynamicUnit.java  is part of robo4j.
 * module: robo4j-core
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.socket.http.units;

import com.robo4j.AttributeDescriptor;
import com.robo4j.BlockingTrait;
import com.robo4j.ConfigurationException;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.channel.InboundHttpSocketChannelHandler;
import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.socket.http.util.CodeRegistryUtils;
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.RoboHttpUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.robo4j.socket.http.util.ChannelBufferUtils.INIT_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_PACKAGES;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_REGISTRY;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_SOCKET_PORT;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_UNIT_PATHS_CONFIG;

/**
 * Http NIO unit allows to configure format of the requests currently is only
 * GET method available. Server currently support IP v4 address
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@BlockingTrait
public class HttpServerUnit extends RoboUnit<Object> {

	public static final String NAME = "httpServer";
	public static final String ATTR_ADDRESS = "address";
	public static final String ATTR_PORT = "serverPort";
	public static final String ATTR_PATHS = "paths";
	public static final AttributeDescriptor<String> DESCRIPTOR_ADDRESS = DefaultAttributeDescriptor.create(String.class,
			ATTR_ADDRESS);
	public static final AttributeDescriptor<Integer> DESCRIPTOR_PORT = DefaultAttributeDescriptor.create(Integer.class,
			ATTR_PORT);
	public static final AttributeDescriptor<String> DESCRIPTOR_PATHS = DefaultAttributeDescriptor.create(String.class,
			ATTR_PATHS);
	public static final Collection<AttributeDescriptor<?>> KNOWN_ATTRIBUTES = Arrays.asList(DESCRIPTOR_ADDRESS,
			DESCRIPTOR_PORT, DESCRIPTOR_PATHS);

	private final ServerContext serverContext = new ServerContext();
	private InboundHttpSocketChannelHandler handler;
	private List<HttpPathMethodDTO> paths;
	private String serverAddress;
	private Integer serverPort;

	public HttpServerUnit(RoboContext context, String id) {
		super(Object.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		serverPort = configuration.getInteger(PROPERTY_SOCKET_PORT, RoboHttpUtils.DEFAULT_PORT);
		serverAddress = configuration.getString(ATTR_ADDRESS, "0.0.0.0");
		int bufferCapacity = configuration.getInteger(PROPERTY_BUFFER_CAPACITY, INIT_BUFFER_CAPACITY);

		paths = JsonUtil.readPathConfig(HttpPathMethodDTO.class, configuration.getString(PROPERTY_UNIT_PATHS_CONFIG, null));

		serverContext.putProperty(PROPERTY_BUFFER_CAPACITY, bufferCapacity);
		serverContext.putProperty(PROPERTY_SOCKET_PORT, serverPort);

		String packages = configuration.getString(PROPERTY_CODEC_PACKAGES, null);

		serverContext.putProperty(PROPERTY_CODEC_REGISTRY, CodeRegistryUtils.getCodecRegistry(packages));

	}

	/**
	 * start updates context by references
	 */
	@Override
	public void start() {
		setState(LifecycleState.STARTING);
		HttpPathUtils.updateHttpServerContextPaths(getContext(), serverContext, paths);
		handler = new InboundHttpSocketChannelHandler(getContext(), serverContext);
		handler.start();
		setState(LifecycleState.STARTED);
	}

	@Override
	public void stop() {
		setState(LifecycleState.STOPPING);
		handler.stop();
		setState(LifecycleState.STOPPED);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
		if (descriptor.getAttributeName().equals(ATTR_ADDRESS) && descriptor.getAttributeType() == String.class) {
			return (R) serverAddress;
		}

		if (descriptor.getAttributeName().equals(ATTR_PORT) && descriptor.getAttributeType() == Integer.class) {
			return (R) serverPort;
		}

		if (descriptor.getAttributeName().equals(ATTR_PATHS) && descriptor.getAttributeType() == String.class) {
			return (R) JsonUtil.toJsonArray(paths);
		}

		return null;
	}

	@Override
	public Collection<AttributeDescriptor<?>> getKnownAttributes() {
		return KNOWN_ATTRIBUTES;
	}
}
