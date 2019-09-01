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

package com.robo4j.hw.rpi.imu.bno;

/**
 * The various types of {@link DataEvent3f}.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum DataEventType {
	//@formatter:off
    NONE                    (-1),
    MAGNETOMETER			(4),
    ACCELEROMETER 			(8),
    ACCELEROMETER_RAW 		(8),
    ACCELEROMETER_LINEAR 	(8),
    GYROSCOPE				(9),
    VECTOR_GAME             (14),
    VECTOR_ROTATION         (14);
    //@formatter:on
	private final int qPoint;

	DataEventType(int qPoint) {
		this.qPoint = qPoint;
	}

	public int getQ() {
		return qPoint;
	}
}
