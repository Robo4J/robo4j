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
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.PropertiesProvider;
import com.robo4j.socket.http.channel.InboundSocketHandler;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.util.StringConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.robo4j.socket.http.util.ChannelBufferUtils.INIT_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_PORT;

/**
 * Http NIO unit allows to configure format of the requests currently is only
 * GET method available
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpServerUnit extends RoboUnit<Object> {
	public static final String PROPERTY_TARGET = "target";
	public static final String PROPERTY_CODEC_REGISTRY = "codecRegistry";
	public static final String CODEC_PACKAGES_CODE = "packages";
	private final HttpCodecRegistry codecRegistry = new HttpCodecRegistry();
	// used for encoded messages
	private List<String> target;
	private final PropertiesProvider propertiesProvider = new PropertiesProvider();
	private InboundSocketHandler inboundSocketHandler;

	public HttpServerUnit(RoboContext context, String id) {
		super(Object.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		/* target is always initiated as the list */
		target = Arrays.asList(configuration.getString(PROPERTY_TARGET, StringConstants.EMPTY)
				.split(RoboHttpUtils.CHAR_COMMA.toString()));
		int port = configuration.getInteger(HTTP_PROPERTY_PORT, RoboHttpUtils.DEFAULT_PORT);
		int bufferCapacity = configuration.getInteger(HTTP_PROPERTY_BUFFER_CAPACITY, INIT_BUFFER_CAPACITY);

		String packages = configuration.getString(CODEC_PACKAGES_CODE, null);
		if (validatePackages(packages)) {
			codecRegistry.scan(Thread.currentThread().getContextClassLoader(), packages.split(","));
		}

		Map<String, Object> targetUnitsMap = JsonUtil.getMapNyJson(configuration.getString("targetUnits", null));

		if(targetUnitsMap.isEmpty()){
			SimpleLoggingUtil.error(getClass(), "no targetUnits");
		} else {
			targetUnitsMap.forEach((key, value) ->
				HttpUriRegister.getInstance().addUnitPathNode(key, value.toString()));
		}
		propertiesProvider.put(HTTP_PROPERTY_BUFFER_CAPACITY, bufferCapacity);
		propertiesProvider.put(HTTP_PROPERTY_PORT, port);
		propertiesProvider.put(PROPERTY_CODEC_REGISTRY, codecRegistry);
	}

	@Override
	public void start() {
		setState(LifecycleState.STARTING);
		//@formatter:off
		final List<RoboReference<Object>> targetRefs = target.stream()
				.map(e -> getContext().getReference(e))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		//@formatter:on

		inboundSocketHandler = new InboundSocketHandler(getContext(), targetRefs, propertiesProvider);
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
