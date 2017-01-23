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

/**
 * The result returned from a message.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboResult<T> {
	// NOTE(Marcus/Jan 22, 2017): I don't think we should have a result of
	// sending a message. Any results should be communicated as messages back to
	// the sender.
	private final RoboUnit<?> source;
	private final T result;

	public RoboResult(RoboUnit<?> source, T result) {
		this.source = source;
		this.result = result;
	}

	public T getResult() {
		return result;
	}

	public RoboUnit<?> getSource() {
		return source;
	}
}
