/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.hw.rpi.i2c.adafruitbackpack;

import java.io.Serializable;
import java.util.Objects;

/**
 * AsciElement represents the element displayed by {@link AlphanumericDevice}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AsciElement implements PackElement, Serializable {
	private Integer position;
	private Character value;
	private Boolean dot;

	public AsciElement(Integer position, Character value, Boolean dot) {
		this.position = position;
		this.value = value;
		this.dot = dot == null ? false : dot;
	}

	public Integer getPosition() {
		return position;
	}

	public Character getValue() {
		return value;
	}

	public boolean getDot() {
		return dot;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		AsciElement that = (AsciElement) o;
		return Objects.equals(position, that.position) && Objects.equals(value, that.value)
				&& Objects.equals(dot, that.dot);
	}

	@Override
	public int hashCode() {
		return Objects.hash(position, value, dot);
	}

	@Override
	public String toString() {
		return "AsciElement{" + "position=" + position + ", value=" + value + ", dot=" + dot + '}';
	}
}
