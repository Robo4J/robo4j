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
public class Matrix4i implements Matrix {
	public int m11;
	public int m12;
	public int m13;
	public int m14;
	public int m21;
	public int m22;
	public int m23;
	public int m24;
	public int m31;
	public int m32;
	public int m33;
	public int m34;
	public int m41;
	public int m42;
	public int m43;
	public int m44;

	public Matrix4i(int m11, int m12, int m13, int m14, int m21, int m22, int m23, int m24, int m31, int m32, int m33,
			int m34, int m41, int m42, int m43, int m44) {
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

	public Matrix4i(int[] matrix) {
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
		int tmp = m12;
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
	public Tuple4i multiply(Tuple4i tuple) {
		int x = m11 * tuple.x + m12 * tuple.y + m13 * tuple.z + m14 * tuple.t;
		int y = m21 * tuple.x + m22 * tuple.y + m23 * tuple.z + m24 * tuple.t;
		int z = m31 * tuple.x + m32 * tuple.y + m33 * tuple.z + m34 * tuple.t;
		int t = m41 * tuple.x + m42 * tuple.y + m43 * tuple.z + m44 * tuple.t;
		return new Tuple4i(x, y, z, t);
	}

	/**
	 * Creates an identity matrix.
	 *
	 * @return Matrix 4D
	 */
	public static Matrix4i createIdentity() {
		return new Matrix4i(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
	}
	
	/**
	 * Returns the value for the row and the column.
	 * 
	 * @param row
	 *            the row
	 * @param column
	 *            the column
	 * @return the value for the row and the column
	 */
	public int getValue(int row, int column) {
		switch (row) {
		case 0:
			switch (column) {
			case 0:
				return m11;
			case 1:
				return m12;
			case 2:
				return m13;
			case 3:
				return m14;
			default:
				throw new IllegalArgumentException("Column does not exist: " + column);
			}
		case 1:
			switch (column) {
			case 0:
				return m21;
			case 1:
				return m22;
			case 2:
				return m23;
			case 3:
				return m24;
			default:
				throw new IllegalArgumentException("Column does not exist: " + column);
			}
		case 2:
			switch (column) {
			case 0:
				return m31;
			case 1:
				return m32;
			case 2:
				return m33;
			case 3:
				return m34;
			default:
				throw new IllegalArgumentException("Column does not exist: " + column);
			}
		case 3:
			switch (column) {
			case 0:
				return m41;
			case 1:
				return m42;
			case 2:
				return m43;
			case 3:
				return m44;
			default:
				throw new IllegalArgumentException("Column does not exist: " + column);
			}
		default:
			throw new IllegalArgumentException("Row does not exist: " + row);
		}
	}

	@Override
	public String toString() {
		return String.format(
				"m11:%d, m12:%d, m13:%d, m14:%d, m21:%d, m22:%d, m23:%d, m24:%d, m31:%d, m32:%d, m33:%d, m34:%d, m41:%d, 42:%d, m43:%d, m44:%d",
				m11, m12, m13, m14, m21, m22, m23, m24, m31, m32, m33, m34, m41, m42, m43, m44);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + m11;
		result = prime * result + m12;
		result = prime * result + m13;
		result = prime * result + m14;
		result = prime * result + m21;
		result = prime * result + m22;
		result = prime * result + m23;
		result = prime * result + m24;
		result = prime * result + m31;
		result = prime * result + m32;
		result = prime * result + m33;
		result = prime * result + m34;
		result = prime * result + m41;
		result = prime * result + m42;
		result = prime * result + m43;
		result = prime * result + m44;
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
		Matrix4i other = (Matrix4i) obj;
		if (m11 != other.m11)
			return false;
		if (m12 != other.m12)
			return false;
		if (m13 != other.m13)
			return false;
		if (m14 != other.m14)
			return false;
		if (m21 != other.m21)
			return false;
		if (m22 != other.m22)
			return false;
		if (m23 != other.m23)
			return false;
		if (m24 != other.m24)
			return false;
		if (m31 != other.m31)
			return false;
		if (m32 != other.m32)
			return false;
		if (m33 != other.m33)
			return false;
		if (m34 != other.m34)
			return false;
		if (m41 != other.m41)
			return false;
		if (m42 != other.m42)
			return false;
		if (m43 != other.m43)
			return false;
		if (m44 != other.m44)
			return false;
		return true;
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
