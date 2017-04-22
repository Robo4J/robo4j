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
 * Gyro unit requires some book keeping per target
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class FixedGyroNotificationEntry extends AbstractNotificationEntry implements GyroNotificationEntry {
	private final Float3D notificationThreshold;

	public FixedGyroNotificationEntry(RoboReference<GyroEvent> target, Float3D notificationThreshold) {
		super(target);
		this.notificationThreshold = notificationThreshold;
	}

	public Float3D getNotificationThreshold() {
		return notificationThreshold;
	}

	@Override
	public boolean isContinuous() {
		return false;
	}

	@Override
	public void addDelta(Float3D data) {
		getDelta().add(data);
		if (shouldNotify(getDelta())) {
			report(getDelta().copy());
		}
	}

	private boolean shouldNotify(Float3D delta) {
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
