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
package com.robo4j.math.geometry;

/**
 * A tuple of doubles.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Tuple4f {
	public float x;
	public float y;
	public float z;
	public float t;

	public Tuple4f() {
	}

	public Tuple4f(float x, float y, float z, float t) {
		set(x, y, z, t);
	}

	public Tuple4f(Tuple4f val) {
		set(val);
	}

	public static Tuple4f createIdentity() {
		return new Tuple4f(1, 1, 1, 1);
	}

	public void set(float x, float y, float z, float t) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.t = t;
	}

	public void set(Tuple4f f) {
		x = f.x;
		y = f.y;
		z = f.z;
		t = f.t;
	}

	public void subtract(Tuple4f f) {
		x -= f.x;
		y -= f.y;
		z -= f.z;
	}

	public void add(Tuple4f f) {
		x += f.x;
		y += f.y;
		z += f.z;
	}

	public void multiply(Tuple4f f) {
		x *= f.x;
		y *= f.y;
		z *= f.z;
	}

	public void multiplyScalar(double f) {
		x *= f;
		y *= f;
		z *= f;
	}

	public Tuple4f diff(Tuple4f f) {
		return new Tuple4f(f.x - x, f.y - y, f.z - z, f.t - t);
	}

	public Tuple4f copy() {
		return new Tuple4f(x, y, z, t);
	}

	public String toString() {
		return String.format("x:%02.4f, y:%02.4f, z:%02.4f, t:%02.4f", x, y, z, t);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(t);
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		result = prime * result + Float.floatToIntBits(z);
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
		Tuple4f other = (Tuple4f) obj;
		if (Float.floatToIntBits(t) != Float.floatToIntBits(other.t))
			return false;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z))
			return false;
		return true;
	}
}
