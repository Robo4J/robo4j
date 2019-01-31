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
 * A three dimensional matrix.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class Matrix3i implements Matrix {

	private int m11;
	private int m12;
	private int m13;
	private int m21;
	private int m22;
	private int m23;
	private int m31;
	private int m32;
	private int m33;

	public Matrix3i(int m11, int m12, int m13, int m21, int m22, int m23, int m31, int m32, int m33) {
		this.m11 = m11;
		this.m12 = m12;
		this.m13 = m13;
		this.m21 = m21;
		this.m22 = m22;
		this.m23 = m23;
		this.m31 = m31;
		this.m32 = m32;
		this.m33 = m33;
	}

	public Matrix3i(int[] matrix) {
		if (matrix.length != 9) {
			throw new IllegalArgumentException("Array argument for Matrix3f must be 9 elements long");
		}
		m11 = matrix[0];
		m12 = matrix[1];
		m13 = matrix[2];
		m21 = matrix[3];
		m22 = matrix[4];
		m23 = matrix[5];
		m31 = matrix[6];
		m32 = matrix[7];
		m33 = matrix[8];
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
		tmp = m23;
		m23 = m32;
		m32 = tmp;
	}

	/**
	 * Transforms the tuple by multiplying with this matrix.
	 *
	 * @param tuple
	 *            the tuple to multiply with this matrix.
	 */
	public void transform(Tuple3i tuple) {
		tuple.set(m11 * tuple.x + m12 * tuple.y + m13 * tuple.z, m21 * tuple.x + m22 * tuple.y + m23 * tuple.z,
				m31 * tuple.x + m32 * tuple.y + m33 * tuple.z);
	}

	/**
	 * Like transform, but creating a new tuple without changing the old one.
	 *
	 * @param tuple
	 *            the tuple to multiply with.
	 * @return the result from multiplying this matrix with the tuple.
	 */
	public Tuple3i multiply(Tuple3i tuple) {
		int x = m11 * tuple.x + m12 * tuple.y + m13 * tuple.z;
		int y = m21 * tuple.x + m22 * tuple.y + m23 * tuple.z;
		int z = m31 * tuple.x + m32 * tuple.y + m33 * tuple.z;
		return new Tuple3i(x, y, z);
	}

	// FIXME: 17.06.17 multiplcaiton of Tuples3i
	public Matrix3i diff(Matrix3i matrix3i) {
		int divBy = 2;
		int r11 = (m11 + matrix3i.m11) / divBy;
		int r12 = (m12 + matrix3i.m12) / divBy;
		int r13 = (m13 + matrix3i.m13) / divBy;
		int r21 = (m21 + matrix3i.m21) / divBy;
		int r22 = (m22 + matrix3i.m22) / divBy;
		int r23 = (m23 + matrix3i.m23) / divBy;
		int r31 = (m31 + matrix3i.m31) / divBy;
		int r32 = (m32 + matrix3i.m32) / divBy;
		int r33 = (m33 + matrix3i.m33) / divBy;
		return new Matrix3i(r11, r12, r13, r21, r22, r23, r31, r32, r33);
	}

	public Matrix3i multiply(Matrix3i matrix3i) {
		int r11 = m11 * matrix3i.m11 + m12 * matrix3i.m21 + m13 * matrix3i.m31;
		int r12 = m11 * matrix3i.m12 + m12 * matrix3i.m22 + m13 * matrix3i.m32;
		int r13 = m11 * matrix3i.m13 + m12 * matrix3i.m23 + m13 * matrix3i.m33;
		int r21 = m21 * matrix3i.m11 + m22 * matrix3i.m21 + m23 * matrix3i.m31;
		int r22 = m21 * matrix3i.m12 + m22 * matrix3i.m22 + m23 * matrix3i.m32;
		int r23 = m21 * matrix3i.m13 + m22 * matrix3i.m23 + m23 * matrix3i.m33;
		int r31 = m31 * matrix3i.m11 + m32 * matrix3i.m21 + m33 * matrix3i.m31;
		int r32 = m31 * matrix3i.m12 + m32 * matrix3i.m22 + m33 * matrix3i.m32;
		int r33 = m31 * matrix3i.m13 + m32 * matrix3i.m23 + m33 * matrix3i.m33;
		return new Matrix3i(r11, r12, r13, r21, r22, r23, r31, r32, r33);
	}

	/**
	 * Returns the value for the row and the column.
	 * 
	 * @param row
	 *            the row
	 * @param column
	 *            the column
	 * @return value for the row and column
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
			default:
				throw new IllegalArgumentException("Column does not exist: " + column);
			}
		default:
			throw new IllegalArgumentException("Row does not exist: " + row);
		}
	}

	@Override
	public String toString() {
		return String.format("m11:%d, m12:%d, m13:%d, m21:%d, m22:%d, m23:%d, m31:%d, m32:%d, m33:%d", m11, m12, m13,
				m21, m22, m23, m31, m32, m33);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + m11;
		result = prime * result + m12;
		result = prime * result + m13;
		result = prime * result + m21;
		result = prime * result + m22;
		result = prime * result + m23;
		result = prime * result + m31;
		result = prime * result + m32;
		result = prime * result + m33;
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
		Matrix3i other = (Matrix3i) obj;
		if (m11 != other.m11)
			return false;
		if (m12 != other.m12)
			return false;
		if (m13 != other.m13)
			return false;
		if (m21 != other.m21)
			return false;
		if (m22 != other.m22)
			return false;
		if (m23 != other.m23)
			return false;
		if (m31 != other.m31)
			return false;
		if (m32 != other.m32)
			return false;
		if (m33 != other.m33)
			return false;
		return true;
	}

	@Override
	public int getRows() {
		return 3;
	}

	@Override
	public int getColumns() {
		return 3;
	}

	@Override
	public Number getNumber(int row, int column) {
		return getValue(row, column);
	}

}
