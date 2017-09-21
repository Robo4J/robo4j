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

package com.robo4j.socket.http.request;

import com.robo4j.AttributeDescriptor;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.HttpByteWrapper;
import com.robo4j.socket.http.HttpHeaderNames;
import com.robo4j.socket.http.HttpMessage;
import com.robo4j.socket.http.HttpMessageWrapper;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.enums.StatusCode;
import com.robo4j.socket.http.enums.SystemPath;
import com.robo4j.socket.http.units.Constants;
import com.robo4j.socket.http.units.HttpUriRegister;
import com.robo4j.socket.http.util.ByteBufferUtils;
import com.robo4j.socket.http.util.HttpMessageUtil;
import com.robo4j.socket.http.util.HttpPathUtil;
import com.robo4j.socket.http.util.RoboHttpUtils;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboRequestCallable implements Callable<RoboResponseProcess> {

	/* currently is supported only one PATH */
	private static final int DEFAULT_PATH_POSITION_0 = 0;
	private static final int DEFAULT_PATH_POSITION_1 = 1;

	private final RoboUnit<?> unit;
	private final ByteBuffer buffer;
	private final DefaultRequestFactory<?> factory;

	public RoboRequestCallable(RoboUnit<?> unit, ByteBuffer buffer, DefaultRequestFactory<Object> factory) {
		assert buffer != null;
		this.unit = unit;
		this.buffer = buffer;
		this.factory = factory;
	}

	@Override
	public RoboResponseProcess call() throws Exception {
		HttpByteWrapper wrapper = ByteBufferUtils.getHttpByteWrapperByByteBuffer(buffer);

		final String[] headerLines = new String(wrapper.getHeader().array()).split("[\r\n]+");
		final String firstLine = RoboHttpUtils.correctLine(headerLines[0]);
		final String[] tokens = firstLine.split(Constants.HTTP_EMPTY_SEP);
		final HttpMethod method = HttpMethod.getByName(tokens[HttpMessageUtil.METHOD_KEY_POSITION]);

		RoboResponseProcess result = new RoboResponseProcess();
		if (method != null) {
			result.setMethod(method);
			final Map<String, String> params = new HashMap<>();

			for (int i = 1; i < headerLines.length; i++) {
				final String[] array = headerLines[i]
						.split(HttpMessageUtil.getHttpSeparator(HttpMessageUtil.HTTP_HEADER_SEP));

				String key = array[HttpMessageUtil.METHOD_KEY_POSITION].toLowerCase();
				String value = array[HttpMessageUtil.URI_VALUE_POSITION].trim();
				params.put(key, value);
			}

			/* parsed http specifics, header */
			final HttpMessage httpMessage = new HttpMessage(method,
					URI.create(tokens[HttpMessageUtil.URI_VALUE_POSITION]),
					HttpVersion.getByValue(tokens[HttpMessageUtil.VERSION_POSITION]), params);

			final List<String> paths = HttpPathUtil.uriStringToPathList(httpMessage.uri().getPath());
			final RoboReference<?> desiredUnit = getRoboReferenceByPath(paths);

			//@formatter:on
			switch (method) {
			case GET:
				/* currently is supported only one path */
				if (desiredUnit != null) {
					AttributeDescriptor<?> attributeDescriptor = getAttributeByQuery(desiredUnit, httpMessage.uri());
					if (attributeDescriptor == null) {
						final Object unitDescription = factory.processGet(desiredUnit,
								paths, new HttpMessageWrapper<>(httpMessage));
						if (unitDescription != null) {
							result.setResult(unitDescription);
						}
					} else {
						result.setResult(factory.processGet(desiredUnit, attributeDescriptor));
					}
				} else if(paths.size() == 0){
					result.setResult(factory.processGet(unit, new HttpMessageWrapper<>(httpMessage)));
				} else {
					SystemPath systemPath = SystemPath.getByPath(paths.get(DEFAULT_PATH_POSITION_0));
					switch (systemPath) {
						case UNITS:
							result.setResult(StatusCode.NOT_FOUND);
							break;
					}
				}
				return result;
			case POST:
				int length = Integer.valueOf(params.get(HttpHeaderNames.CONTENT_LENGTH));
				final StringBuilder jsonSB = new StringBuilder();
				final String postValue = new String(wrapper.getBody().array());
				if (length == postValue.length()) {
					jsonSB.append(postValue);
				} else {
					SimpleLoggingUtil.error(getClass(), "NOT SAME HEADER LENGTH: " + length + " convertedMessage: " + postValue.length());
				}

				if(paths.size() != 0){
					SystemPath systemPath = SystemPath.getByPath(paths.get(DEFAULT_PATH_POSITION_0));
					switch (systemPath){
						case UNITS:
							if(paths.size() == 2) {
								Object respObj  = factory.processPost(desiredUnit, paths,
										new HttpMessageWrapper<>(httpMessage, jsonSB.toString()));
								result.setResult(respObj== null ? StatusCode.NOT_FOUND : respObj);
							} else {
								result.setResult(StatusCode.NOT_FOUND);
							}

					}
				} else {
					result.setResult(StatusCode.NOT_IMPLEMENTED);
				}

				return result;

			default:
				SimpleLoggingUtil.debug(getClass(), "not implemented method: " + method);
			}
		}

		return result;
	}

	// Private Methods
	/**
	 * @param unit  desired unit {@see RoboReference}
	 * @param query URI query attributes
	 * @return specific Attribute
	 */
	private AttributeDescriptor<?> getAttributeByQuery(RoboReference<?> unit, URI query) {
		//@formatter:off
		return unit.getKnownAttributes().stream()
				.filter(a -> a.getAttributeName().equals(query.getRawQuery()))
				.findFirst()
				.orElse(null);
		//@formatter:on
	}

	/**
	 * parse desired path. If no path available. System health state for all units
	 * is returned returned note: currently is supported only one level path
	 *
	 * @param paths
	 *            registered paths by the configuration
	 * @return reference to desired RoboUnit
	 */
	private RoboReference<?> getRoboReferenceByPath(final List<String> paths) {
		if (paths.isEmpty()) {
			return null;
		} else {
			final HttpUriRegister httpUriRegister = HttpUriRegister.getInstance();
			SystemPath systemPath = SystemPath.getByPath(paths.get(DEFAULT_PATH_POSITION_0));
			if (systemPath != null && paths.size() == 2) {
				switch (systemPath) {
				case UNITS:
					return httpUriRegister.getRoboUnitByPath(paths.get(DEFAULT_PATH_POSITION_1));
				}
			}
			return null;
		}

	}

}
