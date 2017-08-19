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
 * Class to request a specific gyro notification.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class GyroRequest {
	public enum GyroAction {
		/**
		 * Performs a calibration. Note that the notification threshold can also
		 * be set, which result in the threshold being set right after
		 * calibration has completed.
		 */
		CALIBRATE, ONCE, CONTINUOUS, STOP
	}

	private final RoboReference<GyroEvent> target;
	private final Tuple3f notificationThreshold;
	private final GyroAction action;

	/**
	 * Constructor.
	 */
	public GyroRequest(RoboReference<GyroEvent> target, GyroAction action, Tuple3f notificationThreshold) {
		this.target = target;
		this.action = action;
		this.notificationThreshold = notificationThreshold;
	}

	/**
	 * The action for the gyro to take.
	 * 
	 * @return the action
	 */
	public GyroAction getAction() {
		return action;
	}

	/**
	 * @return the notification threshold for when to send a message to the
	 *         target.
	 */
	public Tuple3f getNotificationThreshold() {
		return notificationThreshold;
	}

	/**
	 * @return the recipient of the notifications.
	 */
	public RoboReference<GyroEvent> getTarget() {
		return target;
	}
	}
