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

import com.robo4j.core.client.util.RoboHttpUtils;
import com.robo4j.core.configuration.Configuration;

/**
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class StringProducer extends RoboUnit<String> {
	private String target;
	private String method;

	/**
	 * @param context
	 * @param id
	 */
	public StringProducer(RoboContext context, String id) {
		super(context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		target = configuration.getString("target", null);
		if (target == null) {
			throw ConfigurationException.createMissingConfigNameException("target");
		}

		method = configuration.getString("method", null);

	}

	public void sendRandomMessage() {
		final String message = StringToolkit.getRandomMessage(10);
		getContext().getReference(target).sendMessage(message);
	}

	public void sendGetSimpleMessage(String host, String message){
		final String request = method.equals("GET") ? RoboHttpUtils.createGetRequest(host, message) : null;
		getContext().getReference(target).sendMessage(request);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RoboResult<String, Integer> onMessage(String message) {

		if(message == null){
			System.out.println("No Message!");
		} else {

			String[] input = message.split("::");
			switch (input[0]){
				case "sendRandomMessage":
					sendRandomMessage();
					break;
				case "sendGetMessage":
					sendGetSimpleMessage("localhost", input[1].trim());
					break;
				default:
					System.out.println("don't understand message: " + message);

			}
		}
		return null;
	}

}
