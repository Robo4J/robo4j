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

import com.robo4j.ConfigurationException;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.PropertiesProvider;
import com.robo4j.socket.http.channel.InboundSocketHandler;
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.socket.http.util.RoboHttpUtils;

import java.util.List;
import java.util.stream.Collectors;

import static com.robo4j.socket.http.util.ChannelBufferUtils.INIT_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PATHS_CONFIG;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_PORT;
import static com.robo4j.util.Utf8Constant.UTF8_COMMA;

/**
 * Http NIO unit allows to configure format of the requests currently is only
 * GET method available
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpServerUnit extends RoboUnit<Object> {
	public static final String PROPERTY_CODEC_REGISTRY = "codecRegistry";
	public static final String CODEC_PACKAGES_CODE = "packages";
	private final HttpCodecRegistry codecRegistry = new HttpCodecRegistry();
	private final PropertiesProvider propertiesProvider = new PropertiesProvider();

	private ServerPathConfig serverPathConfig;
	private InboundSocketHandler inboundSocketHandler;

	public HttpServerUnit(RoboContext context, String id) {
		super(Object.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		int port = configuration.getInteger(HTTP_PROPERTY_PORT, RoboHttpUtils.DEFAULT_PORT);
		int bufferCapacity = configuration.getInteger(HTTP_PROPERTY_BUFFER_CAPACITY, INIT_BUFFER_CAPACITY);

		String packages = configuration.getString(CODEC_PACKAGES_CODE, null);
		if (validatePackages(packages)) {
			codecRegistry.scan(Thread.currentThread().getContextClassLoader(), packages.split(UTF8_COMMA));
		}

		serverPathConfig = HttpPathUtils.readHttpServerPathConfig(configuration.getString(HTTP_PATHS_CONFIG, null));
//		String configTarget = configuration.getString(HTTP_TARGETS, StringConstants.EMPTY);
//		if(configTarget.equals(StringConstants.EMPTY)){
//			SimpleLoggingUtil.info(getClass(), "no target units available");
//			targets = new ArrayList<>();
//		} else {
//			Map<String, Object> targetUnitsMap = JsonUtil.getMapByJson(configTarget);
//			targets = extractTargetUnits(targetUnitsMap);
//		}



		propertiesProvider.put(HTTP_PROPERTY_BUFFER_CAPACITY, bufferCapacity);
		propertiesProvider.put(HTTP_PROPERTY_PORT, port);
		propertiesProvider.put(PROPERTY_CODEC_REGISTRY, codecRegistry);
	}


	/**
	 * start updates context by references
	 */
	@Override
	public void start() {
		setState(LifecycleState.STARTING);
		//@formatter:off
		// TODO: 1/21/18 (miro) do we need create a new list collection, map is faster ?
		final List<RoboReference<Object>> targetRefs = serverPathConfig.asStream()
				.map(e -> getContext().getReference(e.getRoboUnit()))
				.collect(Collectors.toList());
		//@formatter:on

		inboundSocketHandler = new InboundSocketHandler(getContext(), targetRefs, propertiesProvider);

		// TODO: 1/21/18 (miro) replace it by HttpServerContext
		HttpUriRegister.getInstance().updateUnits(getContext());
		inboundSocketHandler.start();
		setState(LifecycleState.STARTED);
	}

	@Override
	public void stop() {
		setState(LifecycleState.STOPPING);
		inboundSocketHandler.stop();
		setState(LifecycleState.STOPPED);
	}


	private boolean validatePackages(String packages) {
		if (packages == null) {
			return false;
		}
		for (int i = 0; i < packages.length(); i++) {
			char c = packages.charAt(i);
			if (Character.isWhitespace(c)) {
				return false;
			}
		}
		return true;
	}

}
