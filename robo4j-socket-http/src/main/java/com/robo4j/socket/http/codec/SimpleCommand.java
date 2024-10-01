/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.codec;

import java.util.Objects;

/**
 * used for simple http communication
 *
 * json: { "value" : "some_value", "type" : "class_type" }
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SimpleCommand {

	private String value;
	private String type;

	public SimpleCommand(){
	}

	public SimpleCommand(String value) {
		this.value = value;
		this.type = null;
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

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SimpleCommand that = (SimpleCommand) o;
		return Objects.equals(value, that.value) &&
				Objects.equals(type, that.type);
	}

	@Override
	public int hashCode() {

		return Objects.hash(value, type);
	}

	@Override
	public String toString() {
		return "SimpleCommand{" + "value='" + value + '\'' + ", type='" + type + '\'' + '}';
	}
}
