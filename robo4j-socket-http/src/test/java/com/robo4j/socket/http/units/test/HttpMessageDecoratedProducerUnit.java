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
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.socket.http.util.RequestDenominator;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_TARGET;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_UNIT_PATHS_CONFIG;

/**
 * Test unit to produce HttpDecoratedRequest messages
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpMessageDecoratedProducerUnit extends RoboUnit<Integer> {
	private static final int DEFAULT = 0;
	private static final String ATTRIBUTE_MESSAGE_NUMBER = "getNumberOfSentMessages";

	private final ClientContext clientContext = new ClientContext();
	private AtomicInteger counter;
	private String target;
	private String message;

	public HttpMessageDecoratedProducerUnit(RoboContext context, String id) {
		super(Integer.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		target = configuration.getString(HTTP_PROPERTY_TARGET, null);
		message = configuration.getString("message", null);

		List<ClientPathDTO> paths = HttpPathUtils.readPathConfig(ClientPathDTO.class,
				configuration.getString(HTTP_UNIT_PATHS_CONFIG, null));
		HttpPathUtils.updateHttpClientContextPaths(clientContext, paths);
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
		clientContext.getPathConfigs().forEach(pathConfig -> {
			IntStream.range(DEFAULT, number).forEach(i -> {
				RequestDenominator denominator = new RequestDenominator(pathConfig.getMethod(), pathConfig.getPath(),
						HttpVersion.HTTP_1_1);
				HttpDecoratedRequest request = new HttpDecoratedRequest(denominator);
				switch (pathConfig.getMethod()) {
				case GET:
					request.addCallbacks(pathConfig.getCallbacks());
					break;
				case POST:
					request.addMessage(message);
					break;
				default:
					throw new IllegalStateException("not allowed state: " + pathConfig);
				}
				getContext().getReference(target).sendMessage(request);
			});
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
