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

package com.robo4j.socket.http.client.request;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.HttpMessageWrapper;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.units.Constants;
import com.robo4j.socket.http.units.HttpCodecRegistry;
import com.robo4j.socket.http.units.HttpDecoder;
import com.robo4j.socket.http.units.HttpUriRegister;
import com.robo4j.socket.http.util.JsonUtil;

/**
 * Dynamically configurable request factory
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
// TODO discuss how to use URIs
public class RoboRequestFactory implements DefaultRequestFactory<Object> {
	private final HttpCodecRegistry codecRegistry;

	public RoboRequestFactory(final HttpCodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}

	@Override
	public Object processGet(RoboUnit<?> desiredUnit, HttpMessageWrapper<?> wrapper) {
		if (HttpVersion.containsValue(wrapper.message().version()) && !desiredUnit.getContext().getUnits().isEmpty()) {
			final Map<String, Object> unitsMap = desiredUnit.getContext().getUnits().stream()
					.collect(Collectors.toMap(RoboReference::getId, RoboReference::getState));
			return JsonUtil.getJsonByMap(unitsMap);
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
			return result != null ? result : new byte[Constants.DEFAULT_VALUE_0];
		} catch (InterruptedException | ExecutionException e) {
			SimpleLoggingUtil.error(getClass(), "problem", e);
			return new byte[Constants.DEFAULT_VALUE_0];
		}
	}

	@Override
	public Object processGet(final RoboReference<?> desiredReference, final String path,
			final HttpMessageWrapper<?> wrapper) {
		if (HttpVersion.containsValue(wrapper.message().version())) {
			/* currently is supported only */
			final HttpUriRegister register = HttpUriRegister.getInstance();
			if (register.isUnitAvailable(path)) {
				final HttpDecoder<?> decoder = codecRegistry.getDecoder(desiredReference.getMessageType());
				if (decoder != null) {
					StringBuilder sb = new StringBuilder().append("Unit Description").append(Constants.HTTP_NEW_LINE)
							.append("codec is available:").append(Constants.HTTP_NEW_LINE)
							.append("to send command use POST request").append(Constants.HTTP_NEW_LINE)
							.append("example: { \"value\":\"<possible_value>\"}\n\n").append(Constants.HTTP_NEW_LINE)
							.append(Constants.HTTP_NEW_LINE).append("available type: ")
							.append(desiredReference.getMessageType().toGenericString());
					return sb.toString();
				} else {
					SimpleLoggingUtil.error(getClass(), "no decoder available");
					return "no decoder available";
				}
			}
		} else {
			SimpleLoggingUtil.error(getClass(), "processGet is corrupted: " + wrapper);
		}
		return null;
	}

	/**
	 * currently is supported POST message in JSON format
	 *
	 * example: { "value" : "move" }
	 *
	 * @param desiredUnit
	 * @param path
	 * @param wrapper
	 * @return
	 */
	@Override
	public Object processPost(final RoboReference<?> desiredUnit, final String path,
			final HttpMessageWrapper<?> wrapper) {
		if (HttpVersion.containsValue(wrapper.message().version())) {
			final HttpUriRegister register = HttpUriRegister.getInstance();
			if (register.isUnitAvailable(path)) {
				final String json = (String) wrapper.body();
				final HttpDecoder<?> decoder = codecRegistry.getDecoder(desiredUnit.getMessageType());
				if (decoder != null) {
					return decoder.decode(json);
				} else {
					SimpleLoggingUtil.error(getClass(), "no decoder available");
				}
			}
		} else {
			SimpleLoggingUtil.error(getClass(), "processPost is corrupted: " + wrapper);
		}
		return null;
	}

}
