/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This RoboRequestDynamicFactory.java  is part of robo4j.
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

package com.robo4j.socket.http.request;

import com.robo4j.AttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.ResponseDecoderUnitDTO;
import com.robo4j.socket.http.dto.ResponseUnitDTO;
import com.robo4j.socket.http.units.CodecRegistry;
import com.robo4j.socket.http.units.SocketDecoder;
import com.robo4j.socket.http.units.ServerPathConfig;
import com.robo4j.socket.http.util.HttpConstant;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.ReflectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Dynamically configurable request factory
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboRequestFactory implements DefaultRequestFactory<Object> {
	private static final List<HttpMethod> GET_POST_METHODS = Arrays.asList(HttpMethod.GET, HttpMethod.POST);
	private final CodecRegistry codecRegistry;

	public RoboRequestFactory(final CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}

	@Override
	public Object processGet(RoboContext context) {
		if (!context.getUnits().isEmpty()) {

			final List<ResponseUnitDTO> unitList = context.getUnits().stream()
					.map(u -> new ResponseUnitDTO(u.getId(), u.getState())).collect(Collectors.toList());
			return JsonUtil.toJsonArray(unitList);
		} else {
			SimpleLoggingUtil.error(getClass(), "internal error: no units available");
		}
		return null;
	}

	@Override
	public Object processGet(RoboReference<?> desiredReference, AttributeDescriptor<?> attributeDescriptor) {
		Future<?> future = desiredReference.getAttribute(attributeDescriptor);
		try {
			Object result = future.get();
			return result != null ? result : new byte[HttpConstant.DEFAULT_VALUE_0];
		} catch (InterruptedException | ExecutionException e) {
			SimpleLoggingUtil.error(getClass(), "problem", e);
			return new byte[HttpConstant.DEFAULT_VALUE_0];
		}
	}

	@Override
	public Object processGet(ServerPathConfig pathConfig) {
		final SocketDecoder<?, ?> decoder = codecRegistry.getDecoder(pathConfig.getRoboUnit().getMessageType());
		final ResponseDecoderUnitDTO result = new ResponseDecoderUnitDTO();
		result.setId(pathConfig.getRoboUnit().getId());
		result.setCodec(decoder.getDecodedClass().getName());
		result.setMethods(GET_POST_METHODS);
		return ReflectUtils.createJson(result);
	}

	/**
	 * currently is supported POST message in JSON format
	 *
	 * example: { "value" : "move" }
	 *
	 * @param unitReference
	 *            desired unit
	 * @param message
	 *            string message
	 * @return processed object
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object processPost(final RoboReference<?> unitReference, final String message) {
		final SocketDecoder<Object, ?> decoder = codecRegistry.getDecoder(unitReference.getMessageType());
		return decoder != null ? decoder.decode(message) : null;
	}

}
