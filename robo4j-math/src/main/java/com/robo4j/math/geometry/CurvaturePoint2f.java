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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.math.geometry;

/**
 * A point used when establishing curvature in points. Used when doing feature
 * extraction, such as detecting corners.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CurvaturePoint2f extends Point2f {
	private final float curvature;

	private CurvaturePoint2f(float range, float angle, float x, float y, float curvature) {
		super(range, angle, x, y);
		this.curvature = curvature;
	}

	private CurvaturePoint2f(Point2f point, float totalPhi) {
		this(point.getRange(), point.getAngle(), point.getX(), point.getY(), totalPhi);
	}

	public float getCurvature() {
		return curvature;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Float.floatToIntBits(curvature);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CurvaturePoint2f other = (CurvaturePoint2f) obj;
		if (Float.floatToIntBits(curvature) != Float.floatToIntBits(other.curvature))
			return false;
		return true;
	}

	/**
	 * Factory method for creating a point from polar coordinates.
	 * 
	 * @param range
	 *            the range.
	 * @param angle
	 *            the angle.
	 * @param curvature
	 *            the curvature
	 * @return the resulting point.
	 */
	public static CurvaturePoint2f fromPolar(float range, float angle, float curvature) {
		return new CurvaturePoint2f(Point2f.fromPolar(range, angle), curvature);
	}

	/**
	 * Factory method for creating a point from cartesian coordinates.
	 * 
	 * @param x
	 *            the x value.
	 * @param y
	 *            the y value.
	 * @param curvature
	 *            the curvature
	 * @return the resulting point.
	 */
	public static CurvaturePoint2f fromCartesian(float x, float y, float curvature) {
		return new CurvaturePoint2f(Point2f.fromCartesian(x, y), curvature);
	}

	/**
	 * Factory method for creating a curvature point from a Point2f.
	 * 
	 * @param p
	 *            the point.
	 * @param curvature
	 *            the curvature.
	 * @return the resulting point.
	 */
	public static CurvaturePoint2f fromPoint(Point2f p, float curvature) {
		return new CurvaturePoint2f(p, curvature);
	}

}
