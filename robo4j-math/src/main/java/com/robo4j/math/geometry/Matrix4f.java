/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
 * A four dimensional matrix. (The things we do to save the array size field
 * bytes... ;))
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Matrix4f implements Matrix {
	public float m11;
	public float m12;
	public float m13;
	public float m14;
	public float m21;
	public float m22;
	public float m23;
	public float m24;
	public float m31;
	public float m32;
	public float m33;
	public float m34;
	public float m41;
	public float m42;
	public float m43;
	public float m44;

	public Matrix4f(float m11, float m12, float m13, float m14, float m21, float m22, float m23, float m24, float m31, float m32, float m33,
			float m34, float m41, float m42, float m43, float m44) {
		this.m11 = m11;
		this.m12 = m12;
		this.m13 = m13;
		this.m14 = m14;
		this.m21 = m21;
		this.m22 = m22;
		this.m23 = m23;
		this.m24 = m24;
		this.m31 = m31;
		this.m32 = m32;
		this.m33 = m33;
		this.m34 = m34;
		this.m41 = m31;
		this.m42 = m32;
		this.m43 = m33;
		this.m44 = m34;
	}

	public Matrix4f(float[] matrix) {
		if (matrix.length != 16) {
			throw new IllegalArgumentException("Array argument for Matrix3f must be 9 elements long");
		}
		m11 = matrix[0];
		m12 = matrix[1];
		m13 = matrix[2];
		m14 = matrix[3];
		m21 = matrix[4];
		m22 = matrix[5];
		m23 = matrix[6];
		m24 = matrix[7];
		m31 = matrix[8];
		m32 = matrix[9];
		m33 = matrix[10];
		m34 = matrix[11];
		m41 = matrix[12];
		m42 = matrix[13];
		m43 = matrix[14];
		m44 = matrix[15];
	}

	/**
	 * Transforms the tuple by multiplying with this matrix.
	 * 
	 * @param tuple
	 *            the tuple to multiply with this matrix.
	 */
	public void transform(Tuple4f tuple) {
		tuple.set(m11 * tuple.x + m12 * tuple.y + m13 * tuple.z + m14 * tuple.t,
				m21 * tuple.x + m22 * tuple.y + m23 * tuple.z + m24 * tuple.t,
				m31 * tuple.x + m32 * tuple.y + m33 * tuple.z + m34 * tuple.t,
				m41 * tuple.x + m42 * tuple.y + m43 * tuple.z + m44 * tuple.t);
	}

	/**
	 * Transposes the matrix.
	 */
	public void transpose() {
		float tmp = m12;
		m12 = m21;
		m21 = tmp;
		tmp = m13;
		m13 = m31;
		m31 = tmp;
		tmp = m14;
		m14 = m41;
		m41 = tmp;
		tmp = m23;
		m23 = m32;
		m32 = tmp;
		tmp = m24;
		m24 = m42;
		m42 = tmp;
		tmp = m34;
		m34 = m43;
		m43 = tmp;
	}

	/**
	 * Like transform, but creating a new tuple without changing the old one.
	 * 
	 * @param tuple
	 *            the tuple to multiply with.
	 * @return the result from multiplying this matrix with the tuple.
	 */
	public Tuple4f multiply(Tuple4f tuple) {
		float x = m11 * tuple.x + m12 * tuple.y + m13 * tuple.z + m14 * tuple.t;
		float y = m21 * tuple.x + m22 * tuple.y + m23 * tuple.z + m24 * tuple.t;
		float z = m31 * tuple.x + m32 * tuple.y + m33 * tuple.z + m34 * tuple.t;
		float t = m41 * tuple.x + m42 * tuple.y + m43 * tuple.z + m44 * tuple.t;
		return new Tuple4f(x, y, z, t);
	}

	/**
	 * Creates an identity matrix.
	 *
	 * @return Matrix 4D
	 */
	public static Matrix4f createIdentity() {
		return new Matrix4f(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
	}
	
	/**
	 * Returns the value for the row and the column.
	 * 
	 * @param row
	 *            the row
	 * @param column
	 *            the column
	 * @return value for the row and the column
	 */
	public float getValue(int row, int column) {
		switch (row) {
		case 0:
            return switch (column) {
                case 0 -> m11;
                case 1 -> m12;
                case 2 -> m13;
                case 3 -> m14;
                default -> throw new IllegalArgumentException("Column does not exist: " + column);
            };
		case 1:
            return switch (column) {
                case 0 -> m21;
                case 1 -> m22;
                case 2 -> m23;
                case 3 -> m24;
                default -> throw new IllegalArgumentException("Column does not exist: " + column);
            };
		case 2:
            return switch (column) {
                case 0 -> m31;
                case 1 -> m32;
                case 2 -> m33;
                case 3 -> m34;
                default -> throw new IllegalArgumentException("Column does not exist: " + column);
            };
		case 3:
            return switch (column) {
                case 0 -> m41;
                case 1 -> m42;
                case 2 -> m43;
                case 3 -> m44;
                default -> throw new IllegalArgumentException("Column does not exist: " + column);
            };
		default:
			throw new IllegalArgumentException("Row does not exist: " + row);
		}
	}

	@Override
	public String toString() {
		return String.format(
				"m11:%f, m12:%f, m13:%f, m14:%f, m21:%f, m22:%f, m23:%f, m24:%f, m31:%f, m32:%f, m33:%f, m34:%f, m41:%f, 42:%f, m43:%f, m44:%f",
				m11, m12, m13, m14, m21, m22, m23, m24, m31, m32, m33, m34, m41, m42, m43, m44);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(m11);
		result = prime * result + Float.floatToIntBits(m12);
		result = prime * result + Float.floatToIntBits(m13);
		result = prime * result + Float.floatToIntBits(m14);
		result = prime * result + Float.floatToIntBits(m21);
		result = prime * result + Float.floatToIntBits(m22);
		result = prime * result + Float.floatToIntBits(m23);
		result = prime * result + Float.floatToIntBits(m24);
		result = prime * result + Float.floatToIntBits(m31);
		result = prime * result + Float.floatToIntBits(m32);
		result = prime * result + Float.floatToIntBits(m33);
		result = prime * result + Float.floatToIntBits(m34);
		result = prime * result + Float.floatToIntBits(m41);
		result = prime * result + Float.floatToIntBits(m42);
		result = prime * result + Float.floatToIntBits(m43);
		result = prime * result + Float.floatToIntBits(m44);
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
		Matrix4f other = (Matrix4f) obj;
		if (Float.floatToIntBits(m11) != Float.floatToIntBits(other.m11))
			return false;
		if (Float.floatToIntBits(m12) != Float.floatToIntBits(other.m12))
			return false;
		if (Float.floatToIntBits(m13) != Float.floatToIntBits(other.m13))
			return false;
		if (Float.floatToIntBits(m14) != Float.floatToIntBits(other.m14))
			return false;
		if (Float.floatToIntBits(m21) != Float.floatToIntBits(other.m21))
			return false;
		if (Float.floatToIntBits(m22) != Float.floatToIntBits(other.m22))
			return false;
		if (Float.floatToIntBits(m23) != Float.floatToIntBits(other.m23))
			return false;
		if (Float.floatToIntBits(m24) != Float.floatToIntBits(other.m24))
			return false;
		if (Float.floatToIntBits(m31) != Float.floatToIntBits(other.m31))
			return false;
		if (Float.floatToIntBits(m32) != Float.floatToIntBits(other.m32))
			return false;
		if (Float.floatToIntBits(m33) != Float.floatToIntBits(other.m33))
			return false;
		if (Float.floatToIntBits(m34) != Float.floatToIntBits(other.m34))
			return false;
		if (Float.floatToIntBits(m41) != Float.floatToIntBits(other.m41))
			return false;
		if (Float.floatToIntBits(m42) != Float.floatToIntBits(other.m42))
			return false;
		if (Float.floatToIntBits(m43) != Float.floatToIntBits(other.m43))
			return false;
        return Float.floatToIntBits(m44) == Float.floatToIntBits(other.m44);
    }
	
	@Override
	public int getRows() {
		return 4;
	}

	@Override
	public int getColumns() {
		return 4;
	}

	@Override
	public Number getNumber(int row, int column) {
		return getValue(row, column);
	}
}
