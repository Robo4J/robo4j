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
 * A tuple of floats.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Tuple3f {
	public float x;
	public float y;
	public float z;

	public Tuple3f() {
	}

	public Tuple3f(float x, float y, float z) {
		set(x, y, z);
	}

	public Tuple3f(Tuple3f val) {
		set(val);
	}

	public static Tuple3f createIdentity() {
		return new Tuple3f(1, 1, 1);
	}

	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void set(Tuple3f f) {
		x = f.x;
		y = f.y;
		z = f.z;
	}

	public void subtract(Tuple3f f) {
		x -= f.x;
		y -= f.y;
		z -= f.z;
	}

	public void add(Tuple3f f) {
		x += f.x;
		y += f.y;
		z += f.z;
	}

	public void multiply(Tuple3f f) {
		x *= f.x;
		y *= f.y;
		z *= f.z;
	}

	public void multiplyScalar(float f) {
		x *= f;
		y *= f;
		z *= f;
	}

	public Tuple3f diff(Tuple3f f) {
		return new Tuple3f(f.x - x, f.y - y, f.z - z);
	}

	public Tuple3f copy() {
		return new Tuple3f(x, y, z);
	}

	public String toString() {
		return String.format("x:%02.4f, y:%02.4f, z:%02.4f", x, y, z);
	}
}
