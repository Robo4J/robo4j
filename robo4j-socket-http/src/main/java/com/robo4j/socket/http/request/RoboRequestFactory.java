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

import static com.robo4j.socket.http.util.RoboHttpUtils.CHAR_COLON;
import static com.robo4j.socket.http.util.RoboHttpUtils.CHAR_COMMA;
import static com.robo4j.socket.http.util.RoboHttpUtils.CHAR_CURLY_BRACKET_LEFT;
import static com.robo4j.socket.http.util.RoboHttpUtils.CHAR_CURLY_BRACKET_RIGHT;
import static com.robo4j.socket.http.util.RoboHttpUtils.CHAR_QUOTATION_MARK;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.robo4j.AttributeDescriptor;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.HttpMessageWrapper;
import com.robo4j.socket.http.dto.ResponseUnitDTO;
import com.robo4j.socket.http.units.Constants;
import com.robo4j.socket.http.units.HttpCodecRegistry;
import com.robo4j.socket.http.units.HttpDecoder;
import com.robo4j.socket.http.units.HttpUriRegister;
import com.robo4j.socket.http.util.HttpPathUtil;
import com.robo4j.socket.http.util.JsonUtil;

/**
 * Dynamically configurable request factory
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
// TODO discuss how to use URIs
// FIXME: 18.09.17 (miro) simplify
public class RoboRequestFactory implements DefaultRequestFactory<Object> {
	public static final String NO_DECODER_AVAILABLE = "no decoder available";
	private final HttpCodecRegistry codecRegistry;

	public RoboRequestFactory(final HttpCodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}

	@Override
	public Object processGet(RoboUnit<?> desiredUnit) {
		if (!desiredUnit.getContext().getUnits().isEmpty()) {

			final List<ResponseUnitDTO> unitList = desiredUnit.getContext().getUnits().stream()
					.map(u -> new ResponseUnitDTO(u.getId(), u.getState())).collect(Collectors.toList());

			return JsonUtil.getArrayByListResponseUnitDTO(unitList);
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
	public Object processGetByRegisteredPaths(final RoboReference<?> desiredReference, final List<String> paths) {
		/* currently is supported only */
		final HttpUriRegister register = HttpUriRegister.getInstance();
		if (register.isUnitAvailable(HttpPathUtil.pathsToUri(paths))) {
			final HttpDecoder<?> decoder = codecRegistry.getDecoder(desiredReference.getMessageType());
			if (decoder != null) {
				List<String> methods = Arrays.asList("GET", "POST");
				final StringBuilder sb = new StringBuilder(CHAR_CURLY_BRACKET_LEFT).append(CHAR_QUOTATION_MARK).append("id")
						.append(CHAR_QUOTATION_MARK).append(CHAR_COLON).append(CHAR_QUOTATION_MARK).append(desiredReference.getId())
						.append(CHAR_QUOTATION_MARK).append(CHAR_COMMA).append(CHAR_QUOTATION_MARK).append("codec")
						.append(CHAR_QUOTATION_MARK).append(CHAR_COLON).append(CHAR_QUOTATION_MARK)
						.append(desiredReference.getMessageType().getName()).append(CHAR_QUOTATION_MARK).append(",")
						.append(CHAR_QUOTATION_MARK).append("method").append(CHAR_QUOTATION_MARK).append(CHAR_COLON)
						.append(JsonUtil.getArraysByMethodList(methods)).append(CHAR_CURLY_BRACKET_RIGHT);
				return sb.toString();
			} else {
				SimpleLoggingUtil.error(getClass(), "no decoder available");
				return NO_DECODER_AVAILABLE;
			}
		}
		return NO_DECODER_AVAILABLE;
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
	public Object processPost(final RoboReference<?> desiredUnit, final List<String> paths, final HttpMessageWrapper<?> wrapper) {
		final HttpUriRegister register = HttpUriRegister.getInstance();
		if (register.isUnitAvailable(HttpPathUtil.pathsToUri(paths))) {
			final String json = (String) wrapper.body();
			final HttpDecoder<?> decoder = codecRegistry.getDecoder(desiredUnit.getMessageType());
			if (decoder != null) {
				return decoder.decode(json);
			} else {
				SimpleLoggingUtil.error(getClass(), NO_DECODER_AVAILABLE);
			}
		}
		return null;
	}

}
