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
package com.robo4.math.jfr;

import com.oracle.jrockit.jfr.EventDefinition;
import com.oracle.jrockit.jfr.InstantEvent;
import com.oracle.jrockit.jfr.ValueDefinition;
import com.robo4j.math.geometry.Point2D;

/**
 * This is the JFR event definition for a single scan point.
 * 
 * FIXME(Marcus/Jan 13, 2017): When we start supporting JDK 9, we will simply have a multiversion JAR with
 * overrides.
 * 
 * @author Marcus Hirt
 */
@EventDefinition(path = "robo4j/scan/scanpoint2d", name = "Scan Point 2D", description = "An instant event for a scanned point relative to the robot.", stacktrace = false, thread = true)
@SuppressWarnings("deprecation")
public class ScanPoint2DEvent extends InstantEvent {
	@ValueDefinition(name = "X", description = "X value of the point.")
	private float x;

	@ValueDefinition(name = "Y", description = "Y value of the point.")
	private float y;

	@ValueDefinition(name = "Scan ID", description = "The scan with which the point is associated.", relationKey = ScanEvent.RELATIONAL_KEY_SCAN)
	private int scanID;

	static {
		JfrUtils.register(ScanPoint2DEvent.class);
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public int getScanID() {
		return scanID;
	}

	public void setPoint(Point2D point) {
		x = (float) point.getX();
		y = (float) point.getY();
	}

	public void setScanID(int scanID) {
		this.scanID = scanID;
	}
}
