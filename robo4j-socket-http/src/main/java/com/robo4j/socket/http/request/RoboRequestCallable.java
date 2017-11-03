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
import com.robo4j.socket.http.HttpHeaderFieldNames;
import com.robo4j.socket.http.HttpMessage;
import com.robo4j.socket.http.HttpMessageWrapper;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.dto.RoboPathReferenceDTO;
import com.robo4j.socket.http.enums.StatusCode;
import com.robo4j.socket.http.enums.SystemPath;
import com.robo4j.socket.http.units.BufferWrapper;
import com.robo4j.socket.http.units.Constants;
import com.robo4j.socket.http.units.HttpUriRegister;
import com.robo4j.socket.http.util.ByteBufferUtils;
import com.robo4j.socket.http.util.HttpMessageUtil;
import com.robo4j.socket.http.util.HttpPathUtil;
import com.robo4j.socket.http.util.RoboHttpUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboRequestCallable implements Callable<RoboResponseProcess> {

	private static final int PATH_SECOND_LEVEL = 2;
	/* currently is supported only one PATH */
	private static final int DEFAULT_PATH_POSITION_0 = 0;
	private static final int DEFAULT_PATH_POSITION_1 = 1;
	public static final int PATH_DEFAULT_LEVEL = 0;
	public static final int PATH_FIRST_LEVEL = 1;

	private final RoboUnit<?> unit;
	private final BufferWrapper bufferWrapper;
	private final DefaultRequestFactory<?> factory;

	public RoboRequestCallable(RoboUnit<?> unit, BufferWrapper bufferWrapper, DefaultRequestFactory<Object> factory) {
		assert bufferWrapper != null;
		this.unit = unit;
		this.bufferWrapper = bufferWrapper;
		this.factory = factory;
	}

	@Override
	public RoboResponseProcess call() throws Exception {
		HttpByteWrapper wrapper = ByteBufferUtils.getHttpByteWrapperByByteBuffer(bufferWrapper);

		final String[] headerLines = new String(wrapper.getHeader().array()).split("[\r\n]+");
		final String firstLine = RoboHttpUtils.correctLine(headerLines[0]);
		final String[] tokens = firstLine.split(Constants.HTTP_EMPTY_SEP);
		final HttpMethod method = HttpMethod.getByName(tokens[HttpMessageUtil.METHOD_KEY_POSITION]);

		//TODO: (miro) -> separate header and body different processes
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

			switch (method) {
			case GET:
				switch (paths.size()) {
				case PATH_DEFAULT_LEVEL:
					result.setCode(StatusCode.OK);
					result.setResult(factory.processGet(unit) );
					break;
				case PATH_FIRST_LEVEL:
					result.setCode(StatusCode.NOT_IMPLEMENTED);
					break;
				case PATH_SECOND_LEVEL:
					final RoboPathReferenceDTO pathReference = getRoboReferenceByPath(paths);
					switch (pathReference.getPath()) {
					case NONE:
						result.setCode(StatusCode.NOT_FOUND);
						break;
					case UNITS:
						if (pathReference.getRoboReference() == null) {
							result.setCode(StatusCode.NOT_FOUND);
						} else {
							AttributeDescriptor<?> attributeDescriptor = getAttributeByQuery(
									pathReference.getRoboReference(), httpMessage.uri());
							if (attributeDescriptor != null) {
								result.setCode(StatusCode.OK);
								result.setResult(factory.processGet(pathReference.getRoboReference(), attributeDescriptor));
							} else {
								final Object unitDescription = factory
										.processGetByRegisteredPaths(pathReference.getRoboReference(), paths);
								result.setCode(StatusCode.OK);
								result.setResult(unitDescription);
							}
						}
					}
					break;
				default:
				}
				return result;
			case POST:
				int length = Integer.valueOf(params.get(HttpHeaderFieldNames.CONTENT_LENGTH));
				final StringBuilder jsonSB = new StringBuilder();
				final String postValue = new String(wrapper.getBody().array());

				// check header size
				if (length == postValue.length()) {
					jsonSB.append(postValue);

					switch (paths.size()) {
					case PATH_DEFAULT_LEVEL:
					case PATH_FIRST_LEVEL:
						result.setCode(StatusCode.NOT_IMPLEMENTED);
						break;
					case PATH_SECOND_LEVEL:
						final RoboPathReferenceDTO pathReference = getRoboReferenceByPath(paths);
						switch (pathReference.getPath()) {
						case UNITS:
							if (pathReference.getRoboReference() != null) {
								Object respObj = factory.processPost(pathReference.getRoboReference(), paths,
										new HttpMessageWrapper<>(httpMessage, jsonSB.toString()));
								if (respObj != null) {
									result.setCode(StatusCode.ACCEPTED);
									result.setResult(respObj);
								} else {
									result.setCode(StatusCode.NOT_FOUND);
								}
							}
							break;
						case NONE:
						default:
							result.setCode(StatusCode.NOT_FOUND);
						}
						break;
					}

				} else {
					SimpleLoggingUtil.error(getClass(),
							"NOT SAME HEADER LENGTH: " + length + " convertedMessage: " + postValue.length());
					result.setCode(StatusCode.NOT_ACCEPTABLE);
				}

				return result;

			default:
				result.setCode(StatusCode.BAD_REQUEST);
				SimpleLoggingUtil.debug(getClass(), "not implemented method: " + method);
			}
		} else {
			result.setCode(StatusCode.BAD_REQUEST);
		}

		return result;
	}

	// Private Methods
	/**
	 * @param unit
	 *            desired unit {@see RoboReference}
	 * @param query
	 *            URI query attributes
	 * @return specific Attribute
	 */
	private AttributeDescriptor<?> getAttributeByQuery(RoboReference<?> unit, URI query) {
		// @formatter:off
		return unit.getKnownAttributes().stream().filter(a -> a.getAttributeName().equals(query.getRawQuery()))
				.findFirst().orElse(null);
		// @formatter:on
	}

	/**
	 * parse desired path. If no path available. System health state for all units
	 * is returned returned note: currently is supported only one level path
	 *
	 * @param paths
	 *            registered paths by the configuration
	 * @return reference to desired RoboUnit
	 */
	private RoboPathReferenceDTO getRoboReferenceByPath(final List<String> paths) {
		if (paths.isEmpty()) {
			return new RoboPathReferenceDTO(SystemPath.NONE, null);
		} else {
			final SystemPath systemPath = SystemPath.getByPath(paths.get(DEFAULT_PATH_POSITION_0));
			if (systemPath != null && paths.size() == PATH_SECOND_LEVEL) {
				switch (systemPath) {
				case UNITS:
					final HttpUriRegister httpUriRegister = HttpUriRegister.getInstance();
					final RoboReference<?> reference = httpUriRegister
							.getRoboUnitByPath(paths.get(DEFAULT_PATH_POSITION_1));
					return new RoboPathReferenceDTO(systemPath, reference);
				default:
					throw new IllegalArgumentException("Unsupported path " + systemPath);
				}
			}
			return new RoboPathReferenceDTO(SystemPath.NONE, null);
		}

	}

}
