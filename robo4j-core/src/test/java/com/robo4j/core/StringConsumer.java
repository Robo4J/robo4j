/*
 * Copyright (c) 2014, 2017, Miroslav Wengner, Marcus Hirt
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

import com.robo4j.commons.io.RoboContext;
import com.robo4j.commons.io.RoboResult;
import com.robo4j.commons.unit.RoboUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class StringConsumer extends RoboUnit<String> {
	private final List<String> receivedMessages = new ArrayList<>();

	/**
	 * @param context
	 * @param id
	 */
	public StringConsumer(RoboContext context, String id) {
		super(context, id);
	}

	public List<String> getReceivedMessages() {
		return receivedMessages;
	}
	
	@Override
	public RoboResult<String> onMessage(Object message) {
		String str = (String) message;
		receivedMessages.add(str);
		return new RoboResult<String>(this, str);
	}	
}
