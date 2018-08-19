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
 * A two dimensional point.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Point2f {
	/**
	 * Range in meters
	 */
	private final float range;

	/**
	 * Angle in radians
	 */
	private final float angle;

	private final float x;
	private final float y;

	/**
	 * Constructor.
	 * 
	 * @param range
	 *            in meters
	 * @param angle
	 *            in radians
	 */
	Point2f(float range, float angle, float x, float y) {
		this.range = range;
		this.angle = angle;
		this.x = x;
		this.y = y;
	}

	/**
	 * @return the range in meters
	 */
	public float getRange() {
		return range;
	}

	/**
	 * @return the angle in radians.
	 */
	public float getAngle() {
		return angle;
	}

	/**
	 * @return X value, in meters.
	 */
	public float getX() {
		return x;
	}

	/**
	 * @return Y value, in meters.
	 */
	public float getY() {
		return y;
	}

	public boolean closer(Point2f p) {
		return this.range <= p.getRange();
	}

	public boolean farther(Point2f p) {
		return this.range > p.getRange();
	}

	public String toString() {
		return String.format("x:%2.1f, y:%2.1f, range:%2.1f, angle:%2.1f", x, y, range, Math.toDegrees(angle));
	}

	public float distance(Point2f p) {
		double deltaX = p.getX() - x;
		double deltaY = p.getY() - y;
		return (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(angle);
		result = prime * result + Float.floatToIntBits(range);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Point2f))
			return false;
		Point2f other = (Point2f) obj;
		if (Float.floatToIntBits(angle) != Float.floatToIntBits(other.angle))
			return false;
		if (Float.floatToIntBits(range) != Float.floatToIntBits(other.range))
			return false;
		return true;
	}

	/**
	 * A positive value denoting the difference in range to the two points.
	 * 
	 * @param p
	 *            the point to compare with.
	 * @return zero or above.
	 */
	public double rangeDifference(Point2f p) {
		return Math.abs(p.getRange() - getRange());
	}

	/**
	 * Factory method for creating a point from polar coordinates.
	 * 
	 * @param range
	 *            the range.
	 * @param angle
	 *            the angle.
	 * @return the resulting point.
	 */
	public static Point2f fromPolar(float range, float angle) {
		float x = (float) Math.sin(angle) * range;
		float y = (float) Math.cos(angle) * range;
		return new Point2f(range, angle, x, y);
	}

	/**
	 * Factory method for creating a point from cartesian coordinates.
	 * 
	 * @param x
	 *            the x value.
	 * @param y
	 *            the y value.
	 * @return the resulting point.
	 */
	public static Point2f fromCartesian(float x, float y) {
		float range = (float) Math.sqrt(x * x + y * y);
		float angle = (float) Math.atan(x / y);
		return new Point2f(range, angle, x, y);
	}
}
