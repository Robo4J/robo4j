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
}
