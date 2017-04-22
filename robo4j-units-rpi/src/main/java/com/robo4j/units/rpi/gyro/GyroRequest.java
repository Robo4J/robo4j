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
 * Class to request a specific gyro notification.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class GyroRequest {
	private final RoboReference<GyroEvent> target;
	private final boolean calibrate;
	private final boolean continuous;
	private final Float3D notificationThreshold;

	/**
	 * Constructor.
	 */
	public GyroRequest(RoboReference<GyroEvent> target, boolean calibrate, boolean continuous, Float3D notificationThreshold) {
		this.target = target;
		this.calibrate = calibrate;
		this.continuous = continuous;
		this.notificationThreshold = notificationThreshold;
	}

	/**
	 * @return true to force a calibration. Can be paired with setting the
	 *         {@link #getNotificationThreshold()}, which will cause the
	 *         notification threshold to be set as soon as the calibration is
	 *         complete.
	 */
	public boolean calibrate() {
		return calibrate;
	}

	/**
	 * @return the notification threshold for when to send a message to the
	 *         target.
	 */
	public Float3D getNotificationThreshold() {
		return notificationThreshold;
	}

	public RoboReference<GyroEvent> getTarget() {
		return target;
	}

	public boolean isContinuous() {
		return continuous;
	}
}
