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
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.socket.http.message.DatagramDecoratedRequest;
import com.robo4j.socket.http.message.DatagramDenominator;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.message.HttpRequestDenominator;
import com.robo4j.socket.http.units.ClientContext;
import com.robo4j.socket.http.units.ClientPathConfig;
import com.robo4j.socket.http.units.test.enums.CommunicationType;
import com.robo4j.socket.http.util.DatagramBodyType;
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.socket.http.util.JsonUtil;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_TARGET;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_UNIT_PATHS_CONFIG;

/**
 * SocketMessageDecoratedProducerUnit produces HttpDecoratedRequest or DatagramDecoratedRequest messages
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SocketMessageDecoratedProducerUnit extends RoboUnit<Integer> {

	public static final String PROP_COMMUNICATION_TYPE = "communicationType";
	public static final String ATTR_TOTAL_SENT_MESSAGES = "getNumberOfSentMessages";
	public static final String ATTR_MESSAGES_LATCH = "messagesLatch";
	public static final String ATTR_SETUP_LATCH = "setupLatch";
	public static final DefaultAttributeDescriptor<CountDownLatch> DESCRIPTOR_MESSAGES_LATCH = DefaultAttributeDescriptor
			.create(CountDownLatch.class, ATTR_MESSAGES_LATCH);
	public static final DefaultAttributeDescriptor<CountDownLatch> DESCRIPTOR_SETUP_LATCH = DefaultAttributeDescriptor
			.create(CountDownLatch.class, ATTR_SETUP_LATCH);

	private static final int DEFAULT = 0;
	private final ClientContext clientContext = new ClientContext();
	private volatile CountDownLatch setupLatch = new CountDownLatch(1);
	private volatile CountDownLatch messagesLatch;
	private AtomicInteger counter;
	private String target;
	private String message;
	private CommunicationType type;

	public SocketMessageDecoratedProducerUnit(RoboContext context, String id) {
		super(Integer.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		target = configuration.getString(PROPERTY_TARGET, null);
		message = configuration.getString("message", null);

		List<HttpPathMethodDTO> paths = JsonUtil.readPathConfig(HttpPathMethodDTO.class,
				configuration.getString(PROPERTY_UNIT_PATHS_CONFIG, null));
		HttpPathUtils.updateHttpClientContextPaths(clientContext, paths);
		counter = new AtomicInteger(DEFAULT);
		type = CommunicationType.valueOf(configuration.getString(PROP_COMMUNICATION_TYPE, CommunicationType.HTTP.toString()).toUpperCase());
	}

	/**
	 * produces desired number of GET request on RoboSystem
	 * 
	 * @param number
	 *            number of get messages
	 */
	@Override
	public void onMessage(Integer number) {
		messagesLatch = new CountDownLatch(number);
		setupLatch.countDown();
		clientContext.getPathConfigs().forEach(pathConfig -> {
			IntStream.range(DEFAULT, number).forEach(i -> {
				switch (type){
					case HTTP:
						getContext().getReference(target).sendMessage(getHttpRequest(pathConfig));
						break;
					case DATAGRAM:
						getContext().getReference(target).sendMessage(getDatagramRequest(pathConfig));
						break;
					default:
						throw new IllegalStateException("not allowed");
				}
				messagesLatch.countDown();
			});
			System.out.println(getClass().getSimpleName() + "messages: " + number);
		});

	}

	private DatagramDecoratedRequest getDatagramRequest(ClientPathConfig pathConfig){
		final DatagramDenominator denominator = new DatagramDenominator(DatagramBodyType.JSON.getType(), pathConfig.getPath());
		final DatagramDecoratedRequest result = new DatagramDecoratedRequest(denominator);
		result.addMessage(message.getBytes());
		return result;
	}

	private HttpDecoratedRequest getHttpRequest(ClientPathConfig pathConfig){
		HttpRequestDenominator denominator = new HttpRequestDenominator(pathConfig.getMethod(),
				pathConfig.getPath(), HttpVersion.HTTP_1_1);
		HttpDecoratedRequest result = new HttpDecoratedRequest(denominator);
		switch (pathConfig.getMethod()) {
			case GET:
				result.addCallbacks(pathConfig.getCallbacks());
				break;
			case POST:
				result.addMessage(message);
				break;
			default:
				throw new IllegalStateException("not allowed state: " + pathConfig);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
		if (attribute.getAttributeName().equals(ATTR_TOTAL_SENT_MESSAGES)
				&& attribute.getAttributeType() == Integer.class) {
			return (R) (Integer) counter.get();
		}
		if (attribute.getAttributeName().equals(ATTR_MESSAGES_LATCH)
				&& attribute.getAttributeType() == CountDownLatch.class) {
			return (R) messagesLatch;
		}
		if (attribute.getAttributeName().equals(ATTR_SETUP_LATCH)
				&& attribute.getAttributeType() == CountDownLatch.class) {
			return (R) setupLatch;
		}
		return null;
	}

}
