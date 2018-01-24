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

package com.robo4j.socket.http.units.test;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.dto.ClientPathDTO;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.units.ClientContext;
import com.robo4j.socket.http.units.ClientContextBuilder;
import com.robo4j.socket.http.units.ClientPathConfig;
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.socket.http.util.RequestDenominator;
import com.robo4j.util.Utf8Constant;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PATHS_CONFIG;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_HOST;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_PORT;

/**
 * Test unit to produce HttpDecoratedRequest messages
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpMessageDecoratedProducerUnit extends RoboUnit<Integer> {
	private static final int DEFAULT = 0;
	private static final String ATTRIBUTE_MESSAGE_NUMBER = "getNumberOfSentMessages";

	private AtomicInteger counter;
	private String target;
	private String host;
	private Integer port;
	private ClientContext clientContext;

	public HttpMessageDecoratedProducerUnit(RoboContext context, String id) {
		super(Integer.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		target = configuration.getString("target", null);

		List<ClientPathDTO> paths = HttpPathUtils.readPathConfig(ClientPathDTO.class,
				configuration.getString(HTTP_PATHS_CONFIG, null));
		clientContext = HttpPathUtils.initHttpContext(ClientContextBuilder.Builder(), getContext(), paths);

		host = configuration.getString(HTTP_PROPERTY_HOST, null);
		port = configuration.getInteger(HTTP_PROPERTY_PORT, null);
		if (host == null || port == null) {
			throw new ConfigurationException("host, port");
		}
		counter = new AtomicInteger(DEFAULT);
	}

	/**
	 * produces desired number of GET request on RoboSystem
	 * 
	 * @param number
	 *            number of get messages
	 */
	@Override
	public void onMessage(Integer number) {
		IntStream.range(DEFAULT, number).forEach(i -> {
			ClientPathConfig defaultPathConfig = clientContext.getPathConfig(Utf8Constant.UTF8_SOLIDUS);
			RequestDenominator denominator = new RequestDenominator(defaultPathConfig.getMethod(),
					defaultPathConfig.getPath(), HttpVersion.HTTP_1_1);
			HttpDecoratedRequest request = new HttpDecoratedRequest(denominator);
			request.addCallbacks(defaultPathConfig.getCallbacks());
			request.setHost(host);
			request.setPort(port);
			request.addHostHeader();
			getContext().getReference(target).sendMessage(request);
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
		if (attribute.getAttributeName().equals(ATTRIBUTE_MESSAGE_NUMBER)
				&& attribute.getAttributeType() == Integer.class) {
			return (R) (Integer) counter.get();
		}
		return null;
	}

}
