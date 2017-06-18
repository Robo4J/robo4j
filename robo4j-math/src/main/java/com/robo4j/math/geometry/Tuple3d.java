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
public class Tuple3d {
	public double x;
	public double y;
	public double z;

	public Tuple3d() {
	}

	public Tuple3d(double x, double y, double z) {
		set(x, y, z);
	}

	public Tuple3d(Tuple3d val) {
		set(val);
	}

	public static Tuple3d createIdentity() {
		return new Tuple3d(1, 1, 1);
	}

	public void set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void set(Tuple3d f) {
		x = f.x;
		y = f.y;
		z = f.z;
	}

	public void subtract(Tuple3d f) {
		x -= f.x;
		y -= f.y;
		z -= f.z;
	}

	public void add(Tuple3d f) {
		x += f.x;
		y += f.y;
		z += f.z;
	}

	public void multiply(Tuple3d f) {
		x *= f.x;
		y *= f.y;
		z *= f.z;
	}

	public void multiplyScalar(double f) {
		x *= f;
		y *= f;
		z *= f;
	}

	public Tuple3d diff(Tuple3d f) {
		return new Tuple3d(f.x - x, f.y - y, f.z - z);
	}

	public Tuple3d copy() {
		return new Tuple3d(x, y, z);
	}

	public String toString() {
		return String.format("x:%02.4f, y:%02.4f, z:%02.4f", x, y, z);
	}
}
