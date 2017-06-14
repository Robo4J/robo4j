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
package com.robo4j.core.client.request;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.client.util.RoboHttpUtils;
import com.robo4j.core.httpunit.Constants;
import com.robo4j.core.httpunit.HttpUriRegister;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.http.HttpHeaderNames;
import com.robo4j.http.HttpHeaderValues;
import com.robo4j.http.HttpMessage;
import com.robo4j.http.HttpMessageWrapper;
import com.robo4j.http.HttpMethod;
import com.robo4j.http.HttpVersion;
import com.robo4j.http.util.HttpHeaderBuilder;
import com.robo4j.http.util.HttpMessageUtil;
import com.robo4j.http.util.HttpPathUtil;

/**
 * Handling Request
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboRequestCallable implements Callable<Object> {

	/* currently is supported only one PATH */
	private static final int DEFAULT_PATH_POSITION_0 = 0;
	private static final String DEFAULT_RESPONSE = "done";

	private final Socket connection;
	private final DefaultRequestFactory<?> factory;
	private final RoboUnit<?> unit;

	public RoboRequestCallable(RoboUnit<?> unit, Socket connection, DefaultRequestFactory<Object> factory) {
		assert connection != null;
		this.unit = unit;
		this.connection = connection;
		this.factory = factory;
	}

	@Override
	public Object call() throws Exception {
		try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream()));
				BufferedReader in = new BufferedReader(
						new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

			final String firstLine = RoboHttpUtils.correctLine(in.readLine());
			final String[] tokens = firstLine.split(Constants.HTTP_EMPTY_SEP);
			final HttpMethod method = HttpMethod.getByName(tokens[HttpMessageUtil.METHOD_KEY_POSITION]);

			if (method != null) {
				final Map<String, String> params = new HashMap<>();

				String inputLine;
				while (!(inputLine = RoboHttpUtils.correctLine(in.readLine())).equals(Constants.EMPTY_STRING)) {
					final String[] array = inputLine
							.split(HttpMessageUtil.getHttpSeparator(HttpMessageUtil.HTTP_HEADER_SEP));
					params.put(array[HttpMessageUtil.METHOD_KEY_POSITION].toLowerCase(),
							array[HttpMessageUtil.URI_VALUE_POSITION].trim());
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
						processWriter(out, systemSummary.toString());
					} else {
						AttributeDescriptor<?> attributeDescriptor = getAttributeByQuery(desiredUnit,
								httpMessage.uri());
						if (attributeDescriptor == null) {
							final Object unitDescription = factory.processGet(desiredUnit,
									paths.get(DEFAULT_PATH_POSITION_0), new HttpMessageWrapper<>(httpMessage));
							if (unitDescription != null) {
								processWriter(out, unitDescription.toString());
							}
						} else {
							final byte[] unitAttributeValue = (byte[]) factory.processGet(desiredUnit,
									attributeDescriptor);
							processByteWriter(out, unitAttributeValue);
						}
					}
					return DEFAULT_RESPONSE;
				case POST:
					int length = Integer.valueOf(params.get(HttpHeaderNames.CONTENT_LENGTH));
					final StringBuilder jsonSB = new StringBuilder();
					try {
						for (int i = 0; i < length; i++) {
							jsonSB.append((char) in.read());
						}
					} catch (IOException e) {
						SimpleLoggingUtil.error(getClass(), " POST: Problem", e);
					}
					processWriter(out, DEFAULT_RESPONSE);
					return factory.processPost(desiredUnit, paths.get(DEFAULT_PATH_POSITION_0),
							new HttpMessageWrapper<>(httpMessage, jsonSB.toString()));
				default:
					SimpleLoggingUtil.debug(getClass(), "not implemented method: " + method);
					return DEFAULT_RESPONSE;
				}
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
	 * parse desired path. If no path available. System health state for all
	 * units is returned returned note: currently is supported only one level
	 * path
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
			return httpUriRegister.getRoboUnitByPath(paths.get(DEFAULT_PATH_POSITION_0));
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
