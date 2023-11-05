/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
 * A tuple of doubles.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Tuple4d {
	public double x;
	public double y;
	public double z;
	public double t;

	public Tuple4d() {
	}

	public Tuple4d(double x, double y, double z, double t) {
		set(x, y, z, t);
	}

	public Tuple4d(Tuple4d val) {
		set(val);
	}

	public static Tuple4d createIdentity() {
		return new Tuple4d(1, 1, 1, 1);
	}

	public void set(double x, double y, double z, double t) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.t = t;
	}

	public void set(Tuple4d f) {
		x = f.x;
		y = f.y;
		z = f.z;
		t = f.t;
	}

	public void subtract(Tuple4d f) {
		x -= f.x;
		y -= f.y;
		z -= f.z;
	}

	public void add(Tuple4d f) {
		x += f.x;
		y += f.y;
		z += f.z;
	}

	public void multiply(Tuple4d f) {
		x *= f.x;
		y *= f.y;
		z *= f.z;
	}

	public void multiplyScalar(double f) {
		x *= f;
		y *= f;
		z *= f;
	}

	public Tuple4d diff(Tuple4d f) {
		return new Tuple4d(f.x - x, f.y - y, f.z - z, f.t - t);
	}

	public Tuple4d copy() {
		return new Tuple4d(x, y, z, t);
	}

	public String toString() {
		return String.format("x:%02.4f, y:%02.4f, z:%02.4f, t:%02.4f", x, y, z, t);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(t);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		Tuple4d other = (Tuple4d) obj;
		if (Double.doubleToLongBits(t) != Double.doubleToLongBits(other.t))
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
        return Double.doubleToLongBits(z) == Double.doubleToLongBits(other.z);
    }
}
