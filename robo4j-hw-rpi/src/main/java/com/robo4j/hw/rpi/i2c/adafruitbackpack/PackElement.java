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
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class PackElement implements Serializable {
	private static final long serialVersionUID = 1L;

	private final int x;
	private final int y;
	private final BiColor color;

	public PackElement(int x, int y, BiColor color) {
		this.x = x;
		this.y = y;
		this.color = color;
	}

	public PackElement(int x, int y) {
		this.x = x;
		this.y = y;
		this.color = BiColor.OFF;
	}

	public PackElement(int x, BiColor color) {
		this.x = x;
		this.y = 0;
		this.color = color;
	}

	public PackElement(int x) {
		this.x = x;
		this.y = 0;
		this.color = BiColor.OFF;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public BiColor getColor() {
		return color;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PackElement that = (PackElement) o;
		return x == that.x && y == that.y && color == that.color;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, color);
	}

	@Override
	public String toString() {
		return "PackElement{" + "x=" + x + ", y=" + y + ", color=" + color + '}';
	}
}
