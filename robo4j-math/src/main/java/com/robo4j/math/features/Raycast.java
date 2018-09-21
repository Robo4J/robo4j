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
package com.robo4j.math.features;

import java.util.ArrayList;
import java.util.List;

import com.robo4j.math.geometry.CurvaturePoint2f;
import com.robo4j.math.geometry.Point2f;

/**
 * Simple raycasting algorithm.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Raycast {

	/**
	 * Finds the farthest point reachable in a straight line that can be reached
	 * without the vehicle having to adjust course (to avoid touching
	 * something).
	 * 
	 * @param points
	 *            the points to search.
	 * @param noGoRadius
	 *            the radius that the robot should avoid. Make sure it is large
	 *            enough to let the robot pass, with a little margin.
	 * @param raycastStepAngle
	 *            how densely to emit the rays.
	 * @param features
	 *            a previously extracted feature set, so that corners are known.
	 * @return the "optimal" point.
	 */
	public static Point2f raycastFarthestPoint(List<Point2f> points, float noGoRadius, float raycastStepAngle, FeatureSet features) {
		float startAlpha = points.get(0).getAngle();
		float endAlpha = points.get(points.size() - 1).getAngle();

		float resultRange = Float.MIN_VALUE;
		float resultAlpha = 0;

		for (float alpha = startAlpha; alpha <= endAlpha; alpha += raycastStepAngle) {
			float range = raycastSingle(points, features.getCorners(), alpha, noGoRadius);
			if (range == Float.MAX_VALUE) {
				continue;
			}
			if (range > resultRange) {
				resultRange = range;
				resultAlpha = alpha;
			}
		}
		return Point2f.fromPolar(resultRange, resultAlpha);
	}

	/**
	 * Calculates rays, starting in origo. Returns all the points where the rays
	 * hit.
	 * 
	 * @param points
	 *            the points where the rays hit.
	 * @param noGoRadius
	 *            the closest allowable distance to a point.
	 * 
	 * @param raycastStepAngle
	 *            the angle to use between rays.
	 * 
	 * @param features
	 *            the calculated features (e.g. for corner avoidance).
	 * 
	 * @return the points where the rays hit.
	 */
	public static List<Point2f> raycastFull(List<Point2f> points, float noGoRadius, float raycastStepAngle, FeatureSet features) {
		float startAlpha = points.get(0).getAngle();
		float endAlpha = points.get(points.size() - 1).getAngle();

		float approximateNumberOfRays = (endAlpha - startAlpha) / raycastStepAngle;
		List<Point2f> rays = new ArrayList<Point2f>((int) Math.ceil(approximateNumberOfRays));

		for (float alpha = startAlpha; alpha <= endAlpha; alpha += raycastStepAngle) {
			float range = raycastSingle(points, features.getCorners(), alpha, noGoRadius);
			rays.add(Point2f.fromPolar(range, alpha));
		}
		return rays;
	}

	/**
	 * Casts a single ray at the specified angle, and returns the distance at
	 * which it hit something, or {@link Float#MAX_VALUE} if it did not hit
	 * anything.
	 * 
	 * @param points
	 *            the points to check against.
	 * @param corners
	 *            the points known to be corners.
	 * @param rayAlpha
	 *            the angle to emit the ray at.
	 * @param defaultNoGoRadius
	 *            the radius around a known point to avoid.
	 * @return the distance at which the ray hit something.
	 */
	public static float raycastSingle(List<Point2f> points, List<CurvaturePoint2f> corners, float rayAlpha, float defaultNoGoRadius) {
		float minIntersectionRange = Float.MAX_VALUE;
		float currentNoGoRadius = defaultNoGoRadius;
		for (Point2f p : points) {
			CurvaturePoint2f cornerPoint = getMatchingCornerPoint(corners, p);
			if (cornerPoint != null) {
				currentNoGoRadius = calculateNoGoRadius(cornerPoint, defaultNoGoRadius);
			} else {
				currentNoGoRadius = defaultNoGoRadius;
			}
			float tangentDistance = calculateTangentDistance(rayAlpha, p);
			// Fast rejection
			if (Math.abs(tangentDistance) >= currentNoGoRadius) {
				continue;
			}
			float intersectionRange = calculateIntersectionRange(rayAlpha, currentNoGoRadius, tangentDistance, p);
			if (!Float.isNaN(intersectionRange)) {
				minIntersectionRange = Math.min(minIntersectionRange, intersectionRange);
			}
		}
		return minIntersectionRange;
	}

	private static CurvaturePoint2f getMatchingCornerPoint(List<CurvaturePoint2f> corners, Point2f p) {
		for (CurvaturePoint2f cp : corners) {
			if (cp.getX() == p.getX() && cp.getY() == p.getY()) {
				return cp;
			}
		}
		return null;
	}

	public static Point2f raycastAtAngle(List<Point2f> points, float startAngle, float endAngle, float step, float noGoRadius,
			FeatureSet features) {
		float resultRange = Float.MIN_VALUE;
		float resultAlpha = 0;

		for (float alpha = startAngle; alpha <= endAngle; alpha += step) {
			float range = raycastSingle(points, features.getCorners(), alpha, noGoRadius);
			if (range == Float.MAX_VALUE) {
				continue;
			}
			if (range > resultRange) {
				resultRange = range;
				resultAlpha = alpha;
			}
		}
		return Point2f.fromPolar(resultRange, resultAlpha);
	}

	/**
	 * Calculate the range at which a point is hit by the ray.
	 */
	private static float calculateIntersectionRange(float rayAlpha, float noGoRadius, float tangentDistance, Point2f p) {
		float delta = (float) Math.sqrt(noGoRadius * noGoRadius - tangentDistance * tangentDistance);
		if (p.getRange() <= delta) {
			return 0;
		}
		return p.getRange() - delta;
	}

	/**
	 * Calculates the tangential distance.
	 */
	private static float calculateTangentDistance(float rayAlpha, Point2f p) {
		float deltaAlpha = Math.abs(p.getAngle() - rayAlpha);
		if (deltaAlpha >= Math.PI / 2) {
			return Float.MAX_VALUE;
		}
		return (float) (p.getRange() * Math.atan(deltaAlpha));
	}

	/**
	 * Avoid corners a bit more than other points...
	 * 
	 * @param corners
	 */
	private static float calculateNoGoRadius(CurvaturePoint2f point, float noGoRadius) {
		if (point.getCurvature() < 0) {
			return noGoRadius;
		} else {
			return (float) (Math.sin(Math.PI - point.getCurvature()) * noGoRadius * 1.5) + noGoRadius;
		}
	}
}
