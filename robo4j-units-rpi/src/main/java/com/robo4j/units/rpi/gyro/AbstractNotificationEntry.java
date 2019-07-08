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
package com.robo4j.units.rpi.gyro;

import com.robo4j.RoboReference;
import com.robo4j.math.geometry.Tuple3f;

/**
 * This is the abstract base class for notification entries (used for internal
 * book keeping).
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
abstract class AbstractNotificationEntry implements GyroNotificationEntry {
	private final RoboReference<GyroEvent> target;
	private final Tuple3f delta = new Tuple3f();

	/**
	 * Constructor.
	 * 
	 * @param target
	 *            the recipient of the notifications.
	 */
	public AbstractNotificationEntry(RoboReference<GyroEvent> target) {
		this.target = target;
	}

	@Override
	public RoboReference<GyroEvent> getTarget() {
		return target;
	}

	@Override
	public Tuple3f getDelta() {
		return delta;
	}

	/**
	 * Sends an event to the registered receiver of the notification(s).
	 * 
	 * @param angles
	 *            the angles to report.
	 */
	protected void report(Tuple3f angles) {
		getTarget().sendMessage(new GyroEvent(angles));
	}
}