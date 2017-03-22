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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.client.request;

import java.net.URI;

import com.robo4j.core.RoboReference;
import com.robo4j.core.httpunit.HttpCodecRegistry;
import com.robo4j.core.httpunit.HttpDecoder;
import com.robo4j.core.httpunit.HttpUriRegister;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.http.HttpMessageWrapper;
import com.robo4j.http.HttpVersion;

/**
 * Dynamically configurable request factory
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
//TODO discuss how to use URIs
public class RoboRequestFactory implements DefaultRequestFactory<Object> {

	private final HttpCodecRegistry codecRegistry;

	public RoboRequestFactory(final HttpCodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}

	@Override
	public Object processGet(final RoboReference<?> desiredUnit, final String path, final HttpMessageWrapper<?> wrapper) {
		if (HttpVersion.containsValue(wrapper.message().version())) {
			final URI uri = wrapper.message().uri();
			/* currently is supported only */
			final HttpUriRegister register = HttpUriRegister.getInstance();
			if (register.isUnitAvailable(path)) {
				final HttpDecoder<?> decoder = codecRegistry.getDecoder(desiredUnit.getMessageType());
				if(decoder != null){
					return "Unit Description\n" +
							"codec is available:\n" +
							"to send command use POST request\n" +
							"example: { \"value\":\"<possible_value>\"}\n\n" +
							"available type: " + desiredUnit.getMessageType().toGenericString();
				} else {
					SimpleLoggingUtil.error(getClass(), "no decoder available");
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
	public Object processPost(final RoboReference<?> desiredUnit, final String path, final HttpMessageWrapper<?> wrapper) {
		if (HttpVersion.containsValue(wrapper.message().version())) {
			final HttpUriRegister register = HttpUriRegister.getInstance();
			if (register.isUnitAvailable(path)) {
				final String json = new String((char[])wrapper.body());
				final HttpDecoder<?> decoder = codecRegistry.getDecoder(desiredUnit.getMessageType());
				if(decoder != null){
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
