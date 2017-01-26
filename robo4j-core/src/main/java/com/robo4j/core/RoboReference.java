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

import java.util.Map;
/**
 * Reference to a RoboUnit.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
import java.util.concurrent.Future;

public interface RoboReference<T> {
	/**
	 * Sends a message to this RoboUnit.
	 * 
	 * @param message
	 *            the message to send.
	 * @return the RoboUnit specific response.
	 */
	<R> Future<RoboResult<T, R>> sendMessage(Object message);
	
	/**
	 * FIXME: This will have to change!
	 */
	Map<String, String> getConfiguration();
}
