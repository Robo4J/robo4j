/*
 * Copyright (c) 2014-2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.math.geometry;

/**
 * A tuple of four integers.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Tuple4i {
	public int x;
	public int y;
	public int z;
	public int t;

	public Tuple4i() {
	}

	public Tuple4i(int x, int y, int z, int t) {
		set(x, y, z, t);
	}

	public Tuple4i(Tuple4i val) {
		set(val);
	}

	public void set(int x, int y, int z, int t) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.t = t;
	}

	public void set(Tuple4i f) {
		x = f.x;
		y = f.y;
		z = f.z;
		t = f.t;
	}

	public String toString() {
		return String.format("x:%d, y:%d, z:%d, t:%d", x, y, z, t);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + t;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tuple4i other = (Tuple4i) obj;
		if (t != other.t)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (z != other.z)
			return false;
		return true;
	}
}
