package com.robo4j.math.geometry;

/**
 * A two dimensional point.
 * 
 * @author Marcus Hirt
 */
public class Point2D {
	/**
	 * Range in meters
	 */
	private float range;
	
	/**
	 * Angle in radians
	 */
	private float angle;
	
	private float x;
	private float y;
	
	public Point2D(float range, float angle) {
		this.range = range;
		this.angle = angle;
		this.x = (float) Math.sin(angle) * range;
		this.y = (float) Math.cos(angle) * range;
	}
	
	/**
	 * @return the range in meters
	 */
	public float getRange() {
		return range;
	}
	
	public float getAngle() {
		return angle;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	
	public boolean closer(Point2D p) {
		return this.range <= p.getRange();
	}
	
	public boolean farther(Point2D p) {
		return this.range > p.getRange();
	}
	
	public String toString() {
		return String.format("x:%2.1f, y:%2.1f, range:%2.1f, angle:%2.1f", x, y, range, angle);
	}

	public float distance(Point2D p) {
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
		if (!(obj instanceof Point2D))
			return false;
		Point2D other = (Point2D) obj;
		if (Float.floatToIntBits(angle) != Float.floatToIntBits(other.angle))
			return false;
		if (Float.floatToIntBits(range) != Float.floatToIntBits(other.range))
			return false;
		return true;
	}

	/**
	 * A positive value denoting the difference in range to the two points.
	 * 
	 * @param p the point to compare with
	 * @return zero or above.
	 */
	public double rangeDifference(Point2D p) {
		return Math.abs(p.getRange() - getRange());
	}
}
