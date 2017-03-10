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
package com.robo4j.units.rpi.lidarlite;

/**
 * This defines how to perform a scan.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ScanRequest {
	public enum ScanAction {
		STOP, ONCE, CONTINUOUSLY
	}

	private final String receiverId;
	private final ScanAction action;
	private final float startAngle;
	private final float range;
	private final float step;
	private final float abortRange;

	/**
	 * @param receiverId
	 *            the id of the receiver for the result of the scan.
	 * @param servoRange
	 *            how much of the full servo range should be scanned (in
	 *            radians).
	 */
	public ScanRequest(String receiverId, ScanAction action, float startAngle, float range, float step) {
		this(receiverId, action, startAngle, range, step, Float.MAX_VALUE);

	}

	
	/**
	 * @param receiverId
	 *            the id of the receiver for the result of the scan.
	 * @param servoRange
	 *            how much of the full servo range should be scanned (in
	 *            radians).
	 */
	public ScanRequest(String receiverId, ScanAction action, float startAngle, float range, float step, float abortRange) {
		this.receiverId = receiverId;
		this.action = action;
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
	 * @return the kind of {@link ScanAction} to perform.
	 */
	public ScanAction getAction() {
		return action;
	}

	/**
	 * @return the receiver id to receive the result of the scan.
	 */
	public String getReceiverId() {
		return receiverId;
	}


	public float getAbortRange() {
		return abortRange;
	}
}
