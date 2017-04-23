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
package com.robo4j.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.robo4j.core.configuration.Configuration;

/**
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ConfigurationConsumer extends RoboUnit<String> {
	private static final int DEFAULT = 0;
	private AtomicInteger counter;
	private List<String> receivedMessages = new ArrayList<>();

	/**
	 * @param context
	 * @param id
	 */
	public ConfigurationConsumer(RoboContext context, String id) {
		super(String.class, context, id);
		this.counter = new AtomicInteger(DEFAULT);
	}

	public synchronized List<String> getReceivedMessages() {
		return receivedMessages;
	}
	
	@Override
	public synchronized void onMessage(String message) {
		counter.incrementAndGet();
		receivedMessages.add(message);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		System.out.println(configuration);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
		if (attribute.getAttributeName().equals("getNumberOfSentMessages") && attribute.getAttributeType() == Integer.class) {
			return (R) (Integer)counter.get();
		}
		if (attribute.getAttributeName().equals("getReceivedMessages")
				&& attribute.getAttributeType() == ArrayList.class) {
			return (R) receivedMessages;
		}
		return null;
	}

}
