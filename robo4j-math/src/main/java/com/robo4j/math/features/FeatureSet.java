/*
 * Copyright (C) 2017. Miroslav Wengner, Marcus Hirt
 * This FeatureSet.java  is part of robo4j.
 * module: robo4j-math
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.math.features;

import java.util.List;

import com.robo4j.math.geometry.CurvaturePoint2D;
import com.robo4j.math.geometry.Line2D;

/**
 * The features found when doing a feature extraction pass.
 * 
 * @author Marcus Hirt
 */
public class FeatureSet {
	private final List<Line2D> lines;
	private final List<CurvaturePoint2D> corners;
	
	public FeatureSet(List<Line2D> lines, List<CurvaturePoint2D> corners) {
		this.lines = lines;
		this.corners = corners;
	}

	public List<Line2D> getLines() {
		return lines;
	}
	
	public final List<CurvaturePoint2D> getCorners() {
		return corners;
	}
}
