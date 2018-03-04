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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

@SuppressWarnings("rawtypes")
public class StringConsumer extends RoboUnit<String> {
	public static final String NAME = "stringConsumer";
	public static final String PROP_GET_NUMBER_OF_SENT_MESSAGES = "getNumberOfSentMessages";
	public static final String PROP_GET_RECEIVED_MESSAGES = "getReceivedMessages";
	public static final String PROP_COUNT_DOWN_LATCH = "countDownLatch";
	public static final String PROP_COUNT_DOWN_LATCH_COUNT = "countDownLatchCount";
	public static final String PROP_TOTAL_NUMBER_MESSAGES = "totalNumberMessages";

	public static final DefaultAttributeDescriptor<CountDownLatch> DESCRIPTOR_COUNT_DOWN_LATCH = DefaultAttributeDescriptor
			.create(CountDownLatch.class, PROP_COUNT_DOWN_LATCH);
	public static final DefaultAttributeDescriptor<Integer> DESCRIPTOR_MESSAGES_NUMBER_TOTAL = DefaultAttributeDescriptor
			.create(Integer.class, PROP_GET_NUMBER_OF_SENT_MESSAGES);
	public static final DefaultAttributeDescriptor<List> DESCRIPTOR_RECEIVED_MESSAGES = DefaultAttributeDescriptor
			.create(List.class, PROP_GET_RECEIVED_MESSAGES);

	private static final int DEFAULT = 0;
	private AtomicInteger counter;
	private List<String> receivedMessages = new ArrayList<>();
	private CountDownLatch countDownLatch;

	/**
	 * @param context
	 * @param id
	 */
	public StringConsumer(RoboContext context, String id) {
		super(String.class, context, id);
		this.counter = new AtomicInteger(DEFAULT);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		int totalNumber = configuration.getInteger(PROP_TOTAL_NUMBER_MESSAGES, 0);
		if (totalNumber > 0) {
			countDownLatch = new CountDownLatch(totalNumber);
		}
	}

	@Override
	public void onMessage(String message) {
		counter.incrementAndGet();
		System.out.println(getClass().getSimpleName() + ":message:" + message);
		receivedMessages.add(message);
		if (countDownLatch != null) {
			countDownLatch.countDown();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
		if (attribute.getAttributeName().equals(PROP_GET_NUMBER_OF_SENT_MESSAGES)
				&& attribute.getAttributeType() == Integer.class) {
			return (R) (Integer) counter.get();
		}
		if (attribute.getAttributeName().equals(PROP_GET_RECEIVED_MESSAGES)
				&& attribute.getAttributeType() == List.class) {
			return (R) receivedMessages;
		}
		if (attribute.getAttributeName().equals(PROP_COUNT_DOWN_LATCH)
				&& attribute.getAttributeType() == CountDownLatch.class) {
			return (R) countDownLatch;
		}
		if (attribute.getAttributeName().equals(PROP_COUNT_DOWN_LATCH_COUNT)
				&& attribute.getAttributeType() == Long.class) {
			return (R) (countDownLatch == null ? Long.valueOf(0L) : Long.valueOf(countDownLatch.getCount()));
		}
		return null;
	}

}
