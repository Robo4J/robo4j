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

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.HttpByteWrapper;
import com.robo4j.socket.http.HttpHeaderNames;
import com.robo4j.socket.http.HttpHeaderValues;
import com.robo4j.socket.http.HttpMessage;
import com.robo4j.socket.http.HttpMessageWrapper;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.units.Constants;
import com.robo4j.socket.http.units.HttpUriRegister;
import com.robo4j.socket.http.util.ByteBufferUtils;
import com.robo4j.socket.http.util.HttpHeaderBuilder;
import com.robo4j.socket.http.util.HttpMessageUtil;
import com.robo4j.socket.http.util.HttpPathUtil;
import com.robo4j.socket.http.util.RoboHttpUtils;

import java.io.DataOutputStream;
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
public class RoboRequestCallable implements Callable<Object> {

	/* currently is supported only one PATH */
	private static final int DEFAULT_PATH_POSITION_0 = 0;
	private static final String DEFAULT_RESPONSE = "done";

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
	public Object call() throws Exception {
		HttpByteWrapper wrapper = ByteBufferUtils.getHttpByteWrapperByByteBuffer(buffer);


		// FIXME: 27.08.17 (miro) -> review
		final String[] headerLines = new String(wrapper.getHeader().array()).split("[\r\n]+");
		final String firstLine = RoboHttpUtils.correctLine(headerLines[0]);
		final String[] tokens = firstLine.split(Constants.HTTP_EMPTY_SEP);
		final HttpMethod method = HttpMethod.getByName(tokens[HttpMessageUtil.METHOD_KEY_POSITION]);

		if (method != null) {
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
				if (desiredUnit == null) {
					final Object systemSummary = factory.processGet(unit, new HttpMessageWrapper<>(httpMessage));
				} else {
					AttributeDescriptor<?> attributeDescriptor = getAttributeByQuery(desiredUnit, httpMessage.uri());
					if (attributeDescriptor == null) {
						final Object unitDescription = factory.processGet(desiredUnit,
								paths.get(DEFAULT_PATH_POSITION_0), new HttpMessageWrapper<>(httpMessage));
						if (unitDescription != null) {
						}
					} else {
						final byte[] unitAttributeValue = (byte[]) factory.processGet(desiredUnit, attributeDescriptor);
					}
				}
				return DEFAULT_RESPONSE;
			case POST:
				int length = Integer.valueOf(params.get(HttpHeaderNames.CONTENT_LENGTH));
				final StringBuilder jsonSB = new StringBuilder();
				final String postValue = new String(wrapper.getBody().array());
				if (length == postValue.length()) {
					jsonSB.append(postValue);
				} else {
					SimpleLoggingUtil.error(getClass(), "NOT SAME HEADER LENGTH: " + length + " convertedMessage: " + postValue.length());
				}
				return factory.processPost(desiredUnit, paths.get(DEFAULT_PATH_POSITION_0),
						new HttpMessageWrapper<>(httpMessage, jsonSB.toString()));
			default:
				SimpleLoggingUtil.debug(getClass(), "not implemented method: " + method);
				return DEFAULT_RESPONSE;
			}
		}

		return DEFAULT_RESPONSE;
	}

	// Private Methods
	/**
	 *
	 * @param unit
	 *            desired unit {@see RoboReference}
	 * @param query
	 *            URI query attributes
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
			String path = paths.get(DEFAULT_PATH_POSITION_0).replaceFirst("/", "");
			return httpUriRegister.getRoboUnitByPath(path);
		}

	}

	private void processWriter(final DataOutputStream out, String message) throws Exception {
		out.writeBytes(RoboHttpUtils.HTTP_HEADER_OK);
		out.writeBytes(HttpHeaderBuilder.Build().add(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(message.length()))
				.build());
		out.writeBytes(RoboHttpUtils.NEW_LINE);
		out.writeBytes(message);
		out.flush();
	}

	private boolean processByteWriter(final DataOutputStream out, byte[] message) throws Exception {
		out.writeBytes(RoboHttpUtils.HTTP_HEADER_OK);
		out.writeBytes(
				HttpHeaderBuilder.Build().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_IMAGE_JPG)
						.add(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(message.length))
						// .add(HttpHeaderNames.CONTENT_DISPOSITION,
						// HttpHeaderValues.APPLICATION_IMAGE_CONTENT)
						.build());
		out.writeBytes(Constants.HTTP_NEW_LINE);
		out.write(message);
		out.flush();
		return true;
	}
}
