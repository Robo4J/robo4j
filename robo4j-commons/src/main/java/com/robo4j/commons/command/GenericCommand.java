/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This GenericCommand.java  is part of robo4j.
 *
 *  robo4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  robo4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.commons.command;

import com.robo4j.commons.concurrent.TransferSignal;

/**
 * @author Miro Wengner (@miragemiko)
 * @since 10.06.2016
 */
public class GenericCommand<EnumType extends Enum<?>>
		implements RoboUnitCommand, Comparable<GenericCommand>, TransferSignal {

	private CommandProperties properties;
	private EnumType type;
	private String value;
	private int priority;

	public GenericCommand(CommandProperties properties, EnumType type, String value, int priority) {
		this.properties = properties;
		assert type != null;
		this.type = type;
		this.value = value;
		this.priority = priority;
	}

	public CommandProperties getProperties() {
		return properties;
	}

	@Override
	public String getName() {
		return "generic";
	}

	public EnumType getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public int getPriority() {
		return priority;
	}

	@Override
	public int compareTo(GenericCommand o) {
		return (this.priority > o.getPriority()) ? 1 : (this.priority < o.getPriority()) ? -1 : 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof GenericCommand))
			return false;

		GenericCommand<?> that = (GenericCommand<?>) o;

		if (priority != that.priority)
			return false;
		if (properties != null ? !properties.equals(that.properties) : that.properties != null)
			return false;
		if (type != null ? !type.equals(that.type) : that.type != null)
			return false;
		return value != null ? value.equals(that.value) : that.value == null;

	}

	@Override
	public int hashCode() {
		int result = properties != null ? properties.hashCode() : 0;
		result = 31 * result + (type != null ? type.hashCode() : 0);
		result = 31 * result + (value != null ? value.hashCode() : 0);
		result = 31 * result + priority;
		return result;
	}

	@Override
	public String toString() {
		return "GenericCommand{" + "properties=" + properties + ", type=" + type + ", value='" + value + '\''
				+ ", priority=" + priority + '}';
	}
}
