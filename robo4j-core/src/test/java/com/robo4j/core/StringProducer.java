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

import com.robo4j.core.unit.RoboUnit;

import java.util.Map;

/**
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class StringProducer extends RoboUnit<String> {
	private String target;

	/**
	 * @param context
	 * @param id
	 */
	public StringProducer(RoboContext context, String id) {
		super(context, id);
	}

	public void sendRandomMessage() {
		getContext().getReference(target).sendMessage(StringToolkit.getRandomMessage(10));
	}

	@SuppressWarnings("unchecked")
	@Override
	public RoboResult<String, Integer> onMessage(Object message) {
		if (message.equals("sendRandomMessage")) {
			sendRandomMessage();
		} else {
			System.out.println("Do not understand " + message);
		}
		return null;
	}

	@Override
	public void initialize(Map<String, String> properties) throws Exception {
		target = properties.get("target");
		setState(LifecycleState.INITIALIZED);
	}
}
