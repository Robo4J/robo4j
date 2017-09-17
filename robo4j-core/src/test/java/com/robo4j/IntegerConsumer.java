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
package com.robo4j;

import java.util.ArrayList;
import java.util.List;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;

/**
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class IntegerConsumer extends RoboUnit<Integer> {
	private List<Integer> receivedMessages = new ArrayList<>();

	/**
	 * @param context
	 * @param id
	 */
	public IntegerConsumer(RoboContext context, String id) {
		super(Integer.class, context, id);
	}

	public synchronized List<Integer> getReceivedMessages() {
		return receivedMessages;
	}

	@Override
	public synchronized void onMessage(Integer message) {
		receivedMessages.add(message);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {

	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
		if (attribute.getAttributeName().equals("NumberOfReceivedMessages") && attribute.getAttributeType() == Integer.class) {
			return (R) (Integer) receivedMessages.size();
		}
		if (attribute.getAttributeName().equals("ReceivedMessages") && attribute.getAttributeType() == ArrayList.class) {
			return (R) receivedMessages;
		}
		return null;
	}

}
