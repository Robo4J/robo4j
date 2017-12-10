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
import com.robo4j.socket.http.HttpHeaderFieldNames;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.dto.PathMethodDTO;
import com.robo4j.socket.http.message.HttpRequestDescriptor;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.RequestDenominator;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Test unit to produce HttpRequestDescriptor messages
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpMessageDescriptorProducerUnit extends RoboUnit<Integer> {
    private static final int DEFAULT = 0;
	private static final String ATTRIBUTE_MESSAGE_NUMBER = "getNumberOfSentMessages";
    public static final String IP_LOCALHOST = "0.0.0.0";

    private AtomicInteger counter;
	private String target;
	private List<PathMethodDTO> targetPathMethodList;

	public HttpMessageDescriptorProducerUnit(RoboContext context, String id) {
		super(Integer.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		target = configuration.getString("target", null);
		targetPathMethodList = JsonUtil.convertJsonToPathMethodList(configuration.getString("targetUnits", null));
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
			targetPathMethodList.forEach(pathMethod -> {
			    RequestDenominator denominator = new RequestDenominator(pathMethod.getMethod(), pathMethod.getPath(), HttpVersion.HTTP_1_1);
				HttpRequestDescriptor request = new HttpRequestDescriptor(new HashMap<>(), denominator);
				request.addHeaderElement(HttpHeaderFieldNames.HOST, IP_LOCALHOST);
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
