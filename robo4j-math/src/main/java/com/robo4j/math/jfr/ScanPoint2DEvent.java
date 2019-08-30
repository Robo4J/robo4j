/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.math.jfr;

import jdk.jfr.*;
import com.robo4j.math.geometry.Point2f;

/**
 * This is the JFR event definition for a single scan point.
 * 
 * FIXME(Marcus/Jan 13, 2017): When we start supporting JDK 9, we will simply
 * have a multiversion JAR with overrides.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

@Name("robo4j.math.ScanPoint2D")
@Category({ "Robo4J", "Math", "Scan" })
@Label("Scan Point 2D")
@Description("An instant event for a scanned point relative to the scanner")
@StackTrace(false)
public class ScanPoint2DEvent extends Event {
	@Label("X")
	@Description("X value of the point")
	private float x;

	@Label("Y")
	@Description("Y value of the point")
	private float y;

	@Label("Scan Id")
	@Description("The scan with which the point is associated")
	@ScanId
	private int scanId;

	static {
		FlightRecorder.register(ScanPoint2DEvent.class);
	}

	public ScanPoint2DEvent() {
	}

	public ScanPoint2DEvent(Point2f p) {
		setPoint(p);
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public int getScanId() {
		return scanId;
	}

	public void setPoint(Point2f point) {
		x = (float) point.getX();
		y = (float) point.getY();
	}

	public void setScanId(int scanId) {
		this.scanId = scanId;
	}
}
