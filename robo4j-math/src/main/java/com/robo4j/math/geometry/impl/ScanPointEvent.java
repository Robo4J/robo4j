/*
 * Copyright (C) 2017. Miroslav Wengner, Marcus Hirt
 * This ScanPointEvent.java  is part of robo4j.
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

package com.robo4j.math.geometry.impl;

import com.oracle.jrockit.jfr.EventDefinition;
import com.oracle.jrockit.jfr.InstantEvent;
import com.oracle.jrockit.jfr.ValueDefinition;
import com.robo4.math.jfr.JfrUtils;
import com.robo4j.math.geometry.Point2D;

@EventDefinition(path = "coffe/scan/scanpoint", name = "Scan Point", description = "An instant event for a scanned point relative to the robot.", stacktrace = false, thread = true)
@SuppressWarnings("deprecation")
public class ScanPointEvent extends InstantEvent {
	public static final String RELATIONAL_KEY_SCAN = "http://se.hirt.tank/scan";

	@ValueDefinition(name = "X", description = "X value of the point.")
	private float x;

	@ValueDefinition(name = "Y", description = "Y value of the point.")
	private float y;

	@ValueDefinition(name = "Scan ID", description = "The scan with which the point is associated.", relationKey = ScanPointEvent.RELATIONAL_KEY_SCAN)
	private int scanID;

	static {
		JfrUtils.register(ScanPointEvent.class);
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
