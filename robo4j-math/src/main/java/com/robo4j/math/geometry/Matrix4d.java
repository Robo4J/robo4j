/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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
public class Matrix4d implements Matrix {
	public double m11;
	public double m12;
	public double m13;
	public double m14;
	public double m21;
	public double m22;
	public double m23;
	public double m24;
	public double m31;
	public double m32;
	public double m33;
	public double m34;
	public double m41;
	public double m42;
	public double m43;
	public double m44;

	public Matrix4d(double m11, double m12, double m13, double m14, double m21, double m22, double m23, double m24,
			double m31, double m32, double m33, double m34, double m41, double m42, double m43, double m44) {
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

	public Matrix4d(double[] matrix) {
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
	public void transform(Tuple4d tuple) {
		tuple.set(m11 * tuple.x + m12 * tuple.y + m13 * tuple.z + m14 * tuple.t,
				m21 * tuple.x + m22 * tuple.y + m23 * tuple.z + m24 * tuple.t,
				m31 * tuple.x + m32 * tuple.y + m33 * tuple.z + m34 * tuple.t,
				m41 * tuple.x + m42 * tuple.y + m43 * tuple.z + m44 * tuple.t);
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
	public Tuple4d multiply(Tuple4d tuple) {
		double x = m11 * tuple.x + m12 * tuple.y + m13 * tuple.z + m14 * tuple.t;
		double y = m21 * tuple.x + m22 * tuple.y + m23 * tuple.z + m24 * tuple.t;
		double z = m31 * tuple.x + m32 * tuple.y + m33 * tuple.z + m34 * tuple.t;
		double t = m41 * tuple.x + m42 * tuple.y + m43 * tuple.z + m44 * tuple.t;
		return new Tuple4d(x, y, z, t);
	}

	/**
	 * Creates an identity matrix.
	 *
	 * @return Identity 4d Matrix
	 */
	public static Matrix4d createIdentity() {
		return new Matrix4d(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
	}

	/**
	 * Returns the value for the row and the column.
	 * 
	 * @param row
	 *            the row
	 * @param column
	 *            the column
	 * @return value for row and column
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
				"m11:%f, m12:%f, m13:%f, m14:%f, m21:%f, m22:%f, m23:%f, m24:%f, m31:%f, m32:%f, m33:%f, m34:%f, m41:%f, 42:%f, m43:%f, m44:%f",
				m11, m12, m13, m14, m21, m22, m23, m24, m31, m32, m33, m34, m41, m42, m43, m44);
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
		temp = Double.doubleToLongBits(m14);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m21);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m22);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m23);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m24);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m31);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m32);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m33);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m34);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m41);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m42);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m43);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(m44);
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
		Matrix4d other = (Matrix4d) obj;
		if (Double.doubleToLongBits(m11) != Double.doubleToLongBits(other.m11))
			return false;
		if (Double.doubleToLongBits(m12) != Double.doubleToLongBits(other.m12))
			return false;
		if (Double.doubleToLongBits(m13) != Double.doubleToLongBits(other.m13))
			return false;
		if (Double.doubleToLongBits(m14) != Double.doubleToLongBits(other.m14))
			return false;
		if (Double.doubleToLongBits(m21) != Double.doubleToLongBits(other.m21))
			return false;
		if (Double.doubleToLongBits(m22) != Double.doubleToLongBits(other.m22))
			return false;
		if (Double.doubleToLongBits(m23) != Double.doubleToLongBits(other.m23))
			return false;
		if (Double.doubleToLongBits(m24) != Double.doubleToLongBits(other.m24))
			return false;
		if (Double.doubleToLongBits(m31) != Double.doubleToLongBits(other.m31))
			return false;
		if (Double.doubleToLongBits(m32) != Double.doubleToLongBits(other.m32))
			return false;
		if (Double.doubleToLongBits(m33) != Double.doubleToLongBits(other.m33))
			return false;
		if (Double.doubleToLongBits(m34) != Double.doubleToLongBits(other.m34))
			return false;
		if (Double.doubleToLongBits(m41) != Double.doubleToLongBits(other.m41))
			return false;
		if (Double.doubleToLongBits(m42) != Double.doubleToLongBits(other.m42))
			return false;
		if (Double.doubleToLongBits(m43) != Double.doubleToLongBits(other.m43))
			return false;
		if (Double.doubleToLongBits(m44) != Double.doubleToLongBits(other.m44))
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
