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
package com.robo4j.units.rpi.gps;

import com.robo4j.RoboReference;
import com.robo4j.hw.rpi.serial.gps.GPSEvent;

/**
 * Used to register interest in {@link GPSEvent}s.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class GPSRequest {
	private final RoboReference<GPSEvent> target;
	private final Operation operation;

	public enum Operation {
		/**
		 * Will register a listener for GPS information. The information will be
		 * sent to the target.
		 */
		REGISTER,
		/**
		 * Will unregister all GPS notifications to the target. Note that if the
		 * target happens to have several listeners registered, all of them will
		 * be removed.
		 */
		UNREGISTER
	}

	/**
	 * Creates a new GPS request. GPS information will continually be sent to
	 * the target until unregistered.
	 * 
	 * @param target
	 *            the target to send GPS information to, or to unregister from
	 *            getting GPS information.
	 * @param operation
	 * @See {@link Operation}
	 */
	public GPSRequest(RoboReference<GPSEvent> target, Operation operation) {
		this.target = target;
		this.operation = operation;
	}

	/**
	 * The target for the operation.
	 * 
	 * @return the target for the operation.
	 */
	public RoboReference<GPSEvent> getTarget() {
		return target;
	}

	/**
	 * The operation to perform.
	 * 
	 * @see {@link Operation}
	 * @return the operation to perform.
	 */
	public Operation getOperation() {
		return operation;
	}
}
