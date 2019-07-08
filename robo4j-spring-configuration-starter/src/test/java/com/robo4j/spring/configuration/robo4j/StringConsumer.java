/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.spring.configuration.robo4j;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * StringConsumer consumes messages
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class StringConsumer extends RoboUnit<String> {

	public static final String NAME = "consumer";
	public static final String ATTR_GET_RECEIVED_MESSAGES = "getReceivedMessages";
	public static final String ATTR_COUNT_DOWN_LATCH = "countDownLatch";
	@SuppressWarnings("rawtypes")
	public static final DefaultAttributeDescriptor<List> DESCRIPTOR_TOTAL_MESSAGES = DefaultAttributeDescriptor
			.create(List.class, ATTR_GET_RECEIVED_MESSAGES);
	public static final DefaultAttributeDescriptor<CountDownLatch> DESCRIPTOR_COUNT_DOWN_LATCH = DefaultAttributeDescriptor
			.create(CountDownLatch.class, ATTR_COUNT_DOWN_LATCH);
	public static final String ATTR_MESSAGES_NUMBER = "messagesNumber";

	private static final Log log = LogFactory.getLog(StringConsumer.class);
	private CountDownLatch countDownLatch;
	private List<String> messages = new ArrayList<>();

	public StringConsumer(RoboContext context, String id) {
		super(String.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		int messageNumber = configuration.getInteger(ATTR_MESSAGES_NUMBER, null);
		countDownLatch = new CountDownLatch(messageNumber);
	}

	@Override
	public void onMessage(String message) {
		messages.add(message);
		log.debug(message);
		countDownLatch.countDown();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
		if (attribute.getAttributeName().equals(ATTR_GET_RECEIVED_MESSAGES)
				&& attribute.getAttributeType() == List.class) {
			return (R) messages;
		}
		if (attribute.getAttributeName().equals(ATTR_COUNT_DOWN_LATCH)
				&& attribute.getAttributeType() == CountDownLatch.class) {
			return (R) countDownLatch;
		}
		return null;
	}
}
