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

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;

/**
 *
 * Test Controller Test Unit for various commands
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpCommandTestController extends RoboUnit<TestCommandEnum> {

	private String target;

	public HttpCommandTestController(RoboContext context, String id) {
		super(TestCommandEnum.class, context, id);
	}

	@Override
	public void onInitialization(Configuration configuration) throws ConfigurationException {
		target = configuration.getString("target", null);
		if (target == null) {
			throw ConfigurationException.createMissingConfigNameException("target");
		}
	}

	@Override
	public void onMessage(TestCommandEnum message) {
		System.out.println(getClass().getSimpleName() + ": " + message);
		processTestMessage(message);
	}

	// Private Methods
	private void sendMessage(RoboContext ctx, UnitTestMessage message) {
		System.out.println(getClass().getSimpleName() + " message: " + message);
		ctx.getReference(target).sendMessage(message.toString());
	}

	private void processTestMessage(TestCommandEnum myMessage) {
		sendMessage(getContext(), new UnitTestMessage(myMessage));
	}

}
