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

package com.robo4j.socket.http.codec;

import com.robo4j.socket.http.units.Constants;

/**
 * used for simple http communication
 *
 * json: { "value" : "some_value", "type" : "class_type" }
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SimpleCommand {

	private final String value;
	private final String type;

	public SimpleCommand(String value) {
		this.value = value;
		this.type = Constants.EMPTY_STRING;
	}

	/**
	 *
	 * @param value
	 *            command value
	 * @param type
	 *            command class name
	 */
	public SimpleCommand(String value, String type) {
		this.value = value;
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return "SimpleCommand{" + "value='" + value + '\'' + ", type='" + type + '\'' + '}';
	}
}
