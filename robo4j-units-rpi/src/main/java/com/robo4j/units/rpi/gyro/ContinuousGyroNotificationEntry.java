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
package com.robo4j.units.rpi.gyro;

import com.robo4j.core.RoboReference;
import com.robo4j.math.geometry.Float3D;

/**
 * Notification entry for the required book keeping when sending notifications
 * continuously.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class ContinuousGyroNotificationEntry extends AbstractNotificationEntry implements GyroNotificationEntry {
	private final Float3D deltaToNotify;
	private final Float3D lastReported = new Float3D();

	/**
	 * Constructor.
	 * 
	 * @param target
	 *            the recipient of the notifications.
	 * 
	 * @param deltaToNotify
	 *            the delta angles required to send a change. For example (0, 0,
	 *            1) will cause notifications whenever a one degree change has
	 *            been detected around the Z-axis).
	 */
	public ContinuousGyroNotificationEntry(RoboReference<GyroEvent> target, Float3D deltaToNotify) {
		super(target);
		this.deltaToNotify = deltaToNotify;
	}

	@Override
	public boolean isContinuous() {
		return true;
	}

	/**
	 * @return the last reported angles.
	 */
	public Float3D getLastReported() {
		return lastReported;
	}

	/**
	 * @return the delta angle required to notify the target.
	 */
	public Float3D getDeltaToNotify() {
		return deltaToNotify;
	}

	@Override
	public void addDelta(Float3D data) {
		getDelta().add(data);
		Float3D diff = getDelta().diff(lastReported);
		if (Math.abs(diff.x) > deltaToNotify.x || Math.abs(diff.y) > deltaToNotify.y || Math.abs(diff.z) > deltaToNotify.z) {
			Float3D reportedInstance = getDelta().copy();
			lastReported.set(reportedInstance);
			report(reportedInstance);
		}
	}
}
