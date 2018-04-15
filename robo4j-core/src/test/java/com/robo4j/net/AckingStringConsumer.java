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
package com.robo4j.net;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AckingStringConsumer extends RoboUnit<TestMessageType> {
	private static final int DEFAULT = 0;
	public static final String PROP_GET_NUMBER_OF_SENT_MESSAGES = "getNumberOfSentMessages";
	public static final String PROP_GET_RECEIVED_MESSAGES = "getReceivedMessages";
	public static final String PROP_COUNT_DOWN_LATCH = "countDownLatch";
	public static final String PROP_TOTAL_NUMBER_MESSAGES = "totalNumberMessages";
	public static final DefaultAttributeDescriptor<CountDownLatch> DESCRIPTOR_COUNT_DOWN_LATCH = DefaultAttributeDescriptor
			.create(CountDownLatch.class, PROP_COUNT_DOWN_LATCH);
	@SuppressWarnings("rawtypes")
	public static final DefaultAttributeDescriptor<List> DESCRIPTOR_MESSAGES = DefaultAttributeDescriptor
			.create(List.class, PROP_GET_RECEIVED_MESSAGES);
	private AtomicInteger counter;
	private List<TestMessageType> receivedMessages = Collections.synchronizedList(new ArrayList<>());
	private CountDownLatch countDownLatch;

	/**
	 * @param context
	 * @param id
	 */
	public AckingStringConsumer(RoboContext context, String id) {
		super(TestMessageType.class, context, id);
		this.counter = new AtomicInteger(DEFAULT);
	}

	public List<TestMessageType> getReceivedMessages() {
		return receivedMessages;
	}

	@Override
	public void onMessage(TestMessageType message) {
		System.out.println(getClass().getSimpleName() + " onMessage: " + message);
		counter.incrementAndGet();
		receivedMessages.add(message);
		RoboReference<String> ackRef = message.getReference();
		ackRef.sendMessage("acknowledge");
		if(countDownLatch != null){
			countDownLatch.countDown();
		}
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		int totalNumber = configuration.getInteger(PROP_TOTAL_NUMBER_MESSAGES, 0);
		if (totalNumber > 0) {
			countDownLatch = new CountDownLatch(totalNumber);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
		if (attribute.getAttributeName().equals(PROP_GET_NUMBER_OF_SENT_MESSAGES) && attribute.getAttributeType() == Integer.class) {
			return (R) (Integer) counter.get();
		}
		if (attribute.getAttributeName().equals(PROP_GET_RECEIVED_MESSAGES) && attribute.getAttributeType() == List.class) {
			return (R) receivedMessages;
		}
		if (attribute.getAttributeName().equals(PROP_COUNT_DOWN_LATCH)
				&& attribute.getAttributeType() == CountDownLatch.class) {
			return (R) countDownLatch;
		}
		return null;
	}

}
