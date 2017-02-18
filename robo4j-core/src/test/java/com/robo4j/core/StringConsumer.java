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
public class StringConsumer extends RoboUnit<String> {
	private static final int DEFAULT = 0;
	private final List<String> receivedMessages = new ArrayList<>();
	private volatile AtomicInteger counter;

	/**
	 * @param context
	 * @param id
	 */
	public StringConsumer(RoboContext context, String id) {
		super(context, id);
		this.counter = new AtomicInteger(DEFAULT);
	}

	public synchronized List<String> getReceivedMessages() {
		return receivedMessages;
	}
	
	@Override
	public synchronized void onMessage(String message) {
		String str = (String) message;
		int tmpCounter = counter.incrementAndGet();

		System.out.println("StringConsumer: onMessage= " + message + " counter: " + counter.get());
		receivedMessages.add(str);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
		if (attribute.getAttributeName().equals("getNumberOfSentMessages") && attribute.getAttributeType() == Integer.class) {
			return (R) new Integer(getReceivedMessages().size());
		}
		return null;
	}

	public int getCounter(){
		return counter.get();
	}

	public void resetCounter(){
		counter.set(DEFAULT);
	}

}
