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
package com.robo4j.units.rpi.accelerometer;

import java.util.function.Predicate;

import com.robo4j.core.RoboReference;
import com.robo4j.math.geometry.Tuple3f;

/**
 * Class to request a specific accelerometer notification.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AccelerometerRequest {
	private final RoboReference<AccelerometerEvent> target;
	private final boolean continuous;
	private final Predicate<Tuple3f> predicate;

	/**
	 * Constructor.
	 */
	public AccelerometerRequest(RoboReference<AccelerometerEvent> target, boolean continuous, Predicate<Tuple3f> predicate) {
		this.target = target;
		this.continuous = continuous;
		this.predicate = predicate;
	}

	/**
	 * @return the predicate to check to see if a notification should be sent.
	 */
	public Predicate<Tuple3f> getPredicate() {
		return predicate;
	}

	/**
	 * @return the recipient of the notifications.
	 */
	public RoboReference<AccelerometerEvent> getTarget() {
		return target;
	}

	/**
	 * @return true if this is a request for continuous notifications. false, if
	 *         this is a request for a one-off notification.
	 */
	public boolean isContinuous() {
		return continuous;
	}
}
