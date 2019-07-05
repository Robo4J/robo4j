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

import java.util.EnumSet;
import java.util.Objects;

/**
 * XYZAccuracyEvent BNO Accelerometer Event used for Raw, Linear Accelerometer,
 * Gyroscope, Magnetometer
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class XYZAccuracyEvent implements DeviceEvent {

	private static final EnumSet<DeviceEventType> ALLOWED = EnumSet.range(DeviceEventType.MAGNETOMETER,
			DeviceEventType.GYROSCOPE);

	private final DeviceEventType type;
	private final float x;
	private final float y;
	private final float z;
	private final int accuracy;
	private final long timestamp;

	public XYZAccuracyEvent(DeviceEventType type, float x, float y, float z, int accuracy, long timestamp) {
		if (ALLOWED.contains(type)) {
			this.type = type;
			this.x = x;
			this.y = y;
			this.z = z;
			this.accuracy = accuracy;
			this.timestamp = timestamp;
		} else {
			this.type = DeviceEventType.NONE;
			this.x = 0;
			this.y = 0;
			this.z = 0;
			this.accuracy = 0;
			this.timestamp = 0;
		}
	}

	public DeviceEventType getType() {
		return type;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public int getAccuracy() {
		return accuracy;
	}


	@Override
	public long timestampMicro() {
		return timestamp;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		XYZAccuracyEvent that = (XYZAccuracyEvent) o;
		return Float.compare(that.x, x) == 0 &&
				Float.compare(that.y, y) == 0 &&
				Float.compare(that.z, z) == 0 &&
				accuracy == that.accuracy &&
				timestamp == that.timestamp &&
				type == that.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, x, y, z, accuracy, timestamp);
	}

	@Override
	public String toString() {
		return "XYZAccuracyEvent{" +
				"type=" + type +
				", x=" + x +
				", y=" + y +
				", z=" + z +
				", accuracy=" + accuracy +
				", timestamp=" + timestamp +
				'}';
	}
}
