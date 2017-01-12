package com.robo4j.math.geometry;

import java.util.List;
/**
 * The results from a 2D scan.
 * 
 * @author Marcus Hirt
 */
public interface ScanResult2D {
	public List<Point2D> getPoints();
	public Point2D getNearestPoint();
	public Point2D getFarthestPoint();
	public double getMaxX();
	public double getMinX();
	public double getMaxY();
	public double getMinY();
	public int getScanID();
}
