/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.units.rpi.lidarlite;

import com.robo4j.RoboReference;
import com.robo4j.math.geometry.ScanResult2D;

/**
 * This defines how to perform a scan.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ScanRequest {
	private final RoboReference<ScanResult2D> receiver;
	private final float startAngle;
	private final float range;
	private final float step;
	private final float abortRange;

	/**
	 * Constructor.
	 * 
	 * @param startAngle
	 *            the angle relative to the centerline to start from. Note that
	 *            if is more efficient to travel to startAngle + range and scan
	 *            in the reverse direction, that will happen automatically and
	 *            transparently.
	 * 
	 * @param range
	 *            startAngle + range is the other boundary for the scan.
	 * 
	 * @param step
	 *            the angular resolution for the scan.
	 * 
	 * @param receiver
	 *            the reference to the receiver of the scans.
	 */
	public ScanRequest(RoboReference<ScanResult2D> receiver, float startAngle, float range, float step) {
		this(receiver, startAngle, range, step, Float.MAX_VALUE);
	}

	/**
	 * Constructor.
	 * 
	 * @param startAngle
	 *            the angle relative to the centerline to start from. Note that
	 *            if is more efficient to travel to startAngle + range and scan
	 *            in the reverse direction, that will happen automatically and
	 *            transparently.
	 * 
	 * @param range
	 *            startAngle + range is the other boundary for the scan.
	 * 
	 * @param step
	 *            the angular resolution for the scan.
	 * 
	 * @param receiver
	 *            the id of the receiver for the result of the scan.
	 * @param abortRange
	 *            will abort and send, the possibly incomplete, scan early if a
	 *            range measurement is shorter than this value.
	 */
	public ScanRequest(RoboReference<ScanResult2D> receiver, float startAngle, float range, float step,
			float abortRange) {
		this.receiver = receiver;
		this.startAngle = startAngle;
		this.range = range;
		this.step = step;
		this.abortRange = abortRange;
	}

	/**
	 * @return the start angle in degrees for the scan.
	 */
	public float getStartAngle() {
		return startAngle;
	}

	/**
	 * @return the range of the scan, in degrees.
	 */
	public float getRange() {
		return range;
	}

	/**
	 * @return the step, in degrees, between the scans.
	 */
	public float getStep() {
		return step;
	}

	/**
	 * @return the receiver id to receive the result of the scan.
	 */
	public RoboReference<ScanResult2D> getReceiver() {
		return receiver;
	}

	public float getAbortRange() {
		return abortRange;
	}
}
