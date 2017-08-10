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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.units.rpi.gyro;

import com.robo4j.core.RoboReference;
import com.robo4j.math.geometry.Tuple3f;

/**
 * Notification entry for a one-off notification.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class FixedGyroNotificationEntry extends AbstractNotificationEntry implements GyroNotificationEntry {
	private final Tuple3f notificationThreshold;

	/**
	 * Constructor.
	 * 
	 * @param target
	 *            the recipient of the notification.
	 * @param notificationThreshold
	 *            the threshold to be reached for a notification to be sent.
	 */
	public FixedGyroNotificationEntry(RoboReference<GyroEvent> target, Tuple3f notificationThreshold) {
		super(target);
		this.notificationThreshold = notificationThreshold;
	}

	public Tuple3f getNotificationThreshold() {
		return notificationThreshold;
	}

	@Override
	public boolean isContinuous() {
		return false;
	}

	@Override
	public void addDelta(Tuple3f data) {
		getDelta().add(data);
		if (shouldNotify(getDelta())) {
			report(getDelta().copy());
		}
	}

	private boolean shouldNotify(Tuple3f delta) {
		if (notificationThreshold.x != 0) {
			if (checkNotify(notificationThreshold.x, delta.x)) {
				return true;
			}
		}
		if (notificationThreshold.y != 0) {
			if (checkNotify(notificationThreshold.y, delta.y)) {
				return true;
			}
		}
		if (notificationThreshold.z != 0) {
			if (checkNotify(notificationThreshold.z, delta.z)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkNotify(float threshold, float delta) {
		return (threshold > 0 && delta > threshold) || (threshold < 0 && delta < threshold);
	}
}
