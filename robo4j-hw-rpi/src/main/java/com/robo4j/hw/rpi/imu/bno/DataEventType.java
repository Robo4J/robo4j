/*
 * Copyright (c) 2014, 2026, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.hw.rpi.imu.bno;

/**
 * The various types of {@link DataEvent3f}.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum DataEventType {
	//@formatter:off
    NONE                        (-1),
    MAGNETOMETER                (4),
    MAGNETOMETER_UNCALIBRATED   (4),
    ACCELEROMETER               (8),
    ACCELEROMETER_RAW           (8),
    ACCELEROMETER_LINEAR        (8),
    GRAVITY                     (8),
    GYROSCOPE                   (9),
    GYROSCOPE_UNCALIBRATED      (9),
    VECTOR_GAME                 (14),
    VECTOR_ROTATION             (14),
    VECTOR_GEOMAGNETIC          (14),
    VECTOR_ARVR_STABILIZED      (14),
    VECTOR_ARVR_GAME_STABILIZED (14),
    GYRO_INTEGRATED_RV          (14),
    STEP_COUNTER                (0),
    STEP_DETECTOR               (0),
    TAP_DETECTOR                (0),
    STABILITY_CLASSIFIER        (0),
    SHAKE_DETECTOR              (0),
    ACTIVITY_CLASSIFIER         (0);
    //@formatter:on
	private final int qPoint;

	DataEventType(int qPoint) {
		this.qPoint = qPoint;
	}

	public int getQ() {
		return qPoint;
	}
}
