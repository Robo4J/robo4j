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

import java.util.List;

import com.robo4j.math.geometry.CurvaturePoint2f;
import com.robo4j.math.geometry.Line2f;

/**
 * The features found when doing a feature extraction pass.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class FeatureSet {
	private final List<Line2f> lines;
	private final List<CurvaturePoint2f> corners;
	
	public FeatureSet(List<Line2f> lines, List<CurvaturePoint2f> corners) {
		this.lines = lines;
		this.corners = corners;
	}

	public List<Line2f> getLines() {
		return lines;
	}
	
	public final List<CurvaturePoint2f> getCorners() {
		return corners;
	}
}
