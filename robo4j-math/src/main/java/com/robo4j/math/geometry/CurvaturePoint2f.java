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
 * A point used when establishing curvature in points. Used when doing feature
 * extraction, such as detecting corners.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CurvaturePoint2f extends Point2f {
	private final float curvature;

	public CurvaturePoint2f(float range, float angle, float curvature) {
		super(range, angle);
		this.curvature = curvature;
	}

	public CurvaturePoint2f(Point2f point, float totalPhi) {
		this(point.getRange(), point.getAngle(), totalPhi);
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
}
