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
package com.robo4j.math.geometry.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import com.robo4j.math.geometry.Point2f;
import com.robo4j.math.geometry.ScanResult2D;
import com.robo4j.math.jfr.ScanPoint2DEvent;

/**
 * The implementation of a scan result. This particular implementation will emit
 * JFR events to help with the analysis of the recorded JFR data.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ScanResultImpl implements ScanResult2D {
	private static final PointComparator POINT_COMPARATOR = new PointComparator();
	private static final AtomicInteger SCANCOUNTER = new AtomicInteger(0);

	private final List<Point2f> points;

	private double maxX;
	private double minX;
	private double maxY;
	private double minY;
	private int scanID;

	private Point2f farthestPoint;
	private Point2f closestPoint;

	private final ScanPoint2DEvent scanPointEvent = new ScanPoint2DEvent();
	private final float angularResolution;
	private final Predicate<Point2f> pointFilter;

	public ScanResultImpl(float angularResolution, Predicate<Point2f> pointFilter) {
		this(70, angularResolution, pointFilter);
	}

	public ScanResultImpl(int size, float angularResolution, Predicate<Point2f> pointFilter) {
		this.pointFilter = pointFilter;
		scanID = SCANCOUNTER.incrementAndGet();
		this.angularResolution = angularResolution;
		points = new ArrayList<Point2f>(size);
	}

	public double getMaxX() {
		return maxX;
	}

	public double getMinX() {
		return minX;
	}

	public double getMaxY() {
		return maxY;
	}

	public double getMinY() {
		return minY;
	}

	public int getScanID() {
		return scanID;
	}

	public void addPoint(Point2f p) {
		if (!pointFilter.test(p)) {
			return;
		}
		points.add(p);
		emitEvent(p);
		updateBoundaries(p);
	}

	@SuppressWarnings("deprecation")
	private void emitEvent(Point2f p) {
		scanPointEvent.reset();
		scanPointEvent.setPoint(p);
		scanPointEvent.setScanID(scanID);
		scanPointEvent.commit();
	}

	private void updateBoundaries(Point2f p) {
		maxX = Math.max(maxX, p.getX());
		maxY = Math.max(maxY, p.getY());
		minX = Math.min(minX, p.getX());
		minY = Math.min(minY, p.getY());
		if (closestPoint == null) {
			closestPoint = p;
		} else {
			if (p.closer(closestPoint)) {
				closestPoint = p;
			}
		}
		if (farthestPoint == null) {
			farthestPoint = p;
		} else {
			if (p.farther(farthestPoint)) {
				farthestPoint = p;
			}
		}
	}

	public List<Point2f> getPoints() {
		return points;
	}

	/**
	 * Adds a point to the scan.
	 * 
	 * @param range
	 *            range in meters.
	 * @param angle
	 *            angle in radians.
	 */
	public void addPoint(float range, float angle) {
		addPoint(Point2f.fromPolar(range, angle));
	}

	public void addResult(ScanResultImpl result) {
		for (Point2f p : result.getPoints()) {
			addPoint(p);
		}
	}

	public Point2f getNearestPoint() {
		return closestPoint;
	}

	public Point2f getFarthestPoint() {
		return farthestPoint;
	}

	public void sort() {
		Collections.sort(points, POINT_COMPARATOR);
	}

	private static class PointComparator implements Comparator<Point2f> {
		@Override
		public int compare(Point2f o1, Point2f o2) {
			return Float.compare(o1.getAngle(), o2.getAngle());
		}
	}

	public String toString() {
		return String.format("Closest: %s, Farthest: %s, # points: %d", String.valueOf(getNearestPoint()),
				String.valueOf(getFarthestPoint()), getPoints().size());
	}

	@Override
	public float getAngularResolution() {
		return angularResolution;
	}

	@Override
	public Point2f getLeftmostPoint() {
		return points.get(0);
	}

	@Override
	public Point2f getRightmostPoint() {
		// NOTE(Marcus/Sep 5, 2017): Should be fine, as the add phase is
		// separate from the read phase.
		return points.get(points.size() - 1);
	}
}
