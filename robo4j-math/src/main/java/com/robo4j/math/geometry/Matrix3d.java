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
 * A three dimensional matrix.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Matrix3d implements Matrix {
	public double m11;
	public double m12;
	public double m13;
	public double m21;
	public double m22;
	public double m23;
	public double m31;
	public double m32;
	public double m33;

	public Matrix3d(double m11, double m12, double m13, double m21, double m22, double m23, double m31, double m32, double m33) {
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

	public Matrix3d(double[] matrix) {
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
	 * Transforms the tuple by multiplying with this matrix.
	 * 
	 * @param tuple
	 *            the tuple to multiply with this matrix.
	 */
	public void transform(Tuple3d tuple) {
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
	public Tuple3d multiply(Tuple3d tuple) {
		double x = m11 * tuple.x + m12 * tuple.y + m13 * tuple.z;
		double y = m21 * tuple.x + m22 * tuple.y + m23 * tuple.z;
		double z = m31 * tuple.x + m32 * tuple.y + m33 * tuple.z;
		return new Tuple3d(x, y, z);
	}

	/**
	 * Transposes the matrix.
	 */
	public void transpose() {
		double tmp = m12;
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
	 * Creates an identity matrix.
	 */
	public static Matrix3d createIdentity() {
		return new Matrix3d(1, 0, 0, 0, 1, 0, 0, 0, 1);
	}

	/**
	 * Returns the value for the row and the column.
	 * 
	 * @param row
	 *            the row
	 * @param column
	 *            the column
	 * @return
	 */
	public double getValue(int row, int column) {
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
		return String.format("m11:%f, m12:%f, m13:%f, m21:%f, m22:%f, m23:%f, m31:%f, m32:%f, m33:%f", m11, m12, m13, m21, m22, m23, m31,
				m32, m33);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(m11);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m12);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m13);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m21);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m22);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m23);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m31);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m32);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m33);
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
		Matrix3d other = (Matrix3d) obj;
		if (Double.doubleToLongBits(m11) != Double.doubleToLongBits(other.m11))
			return false;
		if (Double.doubleToLongBits(m12) != Double.doubleToLongBits(other.m12))
			return false;
		if (Double.doubleToLongBits(m13) != Double.doubleToLongBits(other.m13))
			return false;
		if (Double.doubleToLongBits(m21) != Double.doubleToLongBits(other.m21))
			return false;
		if (Double.doubleToLongBits(m22) != Double.doubleToLongBits(other.m22))
			return false;
		if (Double.doubleToLongBits(m23) != Double.doubleToLongBits(other.m23))
			return false;
		if (Double.doubleToLongBits(m31) != Double.doubleToLongBits(other.m31))
			return false;
		if (Double.doubleToLongBits(m32) != Double.doubleToLongBits(other.m32))
			return false;
		if (Double.doubleToLongBits(m33) != Double.doubleToLongBits(other.m33))
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
