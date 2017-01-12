/*
 * Copyright (C) 2016, Marcus Hirt
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
 * 3D vector of floats.
 *  
 * @author Marcus Hirt
 */
public class Float3D {
	public float x;
	public float y;
	public float z;

	public Float3D() {
	}

	public Float3D(float x, float y, float z) {
		set(x, y, z);
	}

	public Float3D(Float3D val) {
		set(val);
	}

	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public String toString() {
		return String.format("x:%2.3f, y:%2.3f, z:%2.3f", x, y, z);
	}

	public void subtract(Float3D f) {
		x -= f.x;
		y -= f.y;
		z -= f.z;
	}

	public void add(Float3D f) {
		x += f.x;
		y += f.y;
		z += f.z;
	}

	public void multiply(Float3D f) {
		x *= f.x;
		y *= f.y;
		z *= f.z;
	}

	public void set(Float3D f) {
		x = f.x;
		y = f.y;
		z = f.z;	
	}

	public static Float3D createIdentity() {
		return new Float3D(1, 1, 1);
	}

	public void multiplyScalar(float f) {
		x *= f;
		y *= f;
		z *= f;		
	}

	public Float3D diff(Float3D f) {
		return new Float3D(f.x - x, f.y - y, f.z - z);
	}
	
	public Float3D copy() {
		return new Float3D(x, y, z);
	}
}
