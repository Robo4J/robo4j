/*
 * Copyright (C) 2014-2017, Miroslav Wengner, Marcus Hirt
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

import java.util.List;
/**
 * The results from a 2D scan.
 * 
 * @author Marcus Hirt
 */
public interface ScanResult2D {
	List<Point2D> getPoints();
	Point2D getNearestPoint();
	Point2D getFarthestPoint();
	double getMaxX();
	double getMinX();
	double getMaxY();
	double getMinY();
	int getScanID();
}
