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

import com.robo4j.math.geometry.Tuple3f;

import java.util.EnumSet;
import java.util.Objects;

/**
 * VectorEvent used for Rotation, Game vector event
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class VectorEvent implements DeviceEvent {
	private static final long serialVersionUID = 1L;
	private static final EnumSet<DeviceEventType> ALLOWED = EnumSet.of(DeviceEventType.VECTOR_ROTATION, DeviceEventType.VECTOR_GAME);
	private final DeviceEventType type;
	private final int status;
	private final Tuple3f data;
	private final float quatReal;
	private final float radianAccuracy;
	private final long timestamp;

	public VectorEvent(DeviceEventType type, int status, Tuple3f data, float quatReal, float radianAccuracy, long timestamp) {
		if (ALLOWED.contains(type)) {
			this.status = status;
			this.type = type;
			this.data = data;
			this.quatReal = quatReal;
			this.radianAccuracy = radianAccuracy;
			this.timestamp = timestamp;
		} else {
			this.type = DeviceEventType.NONE;
			this.status = 0;
			this.data = null;
			this.quatReal = 0;
			this.radianAccuracy = 0;
			this.timestamp = 0;
		}

	}

	public DeviceEventType getType() {
		return type;
	}

	public int getStatus() {
		return status;
	}

	public Tuple3f getData() {
		return data;
	}

	public float getQuatReal() {
		return quatReal;
	}

	public float getRadianAccuracy() {
		return radianAccuracy;
	}

	@Override
	public long timestampMicro() {
		return timestamp;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		VectorEvent that = (VectorEvent) o;
		return status == that.status && Float.compare(that.quatReal, quatReal) == 0
				&& Float.compare(that.radianAccuracy, radianAccuracy) == 0 && timestamp == that.timestamp && type == that.type
				&& Objects.equals(data, that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, status, data, quatReal, radianAccuracy, timestamp);
	}

	@Override
	public String toString() {
		return "VectorEvent{" + "type=" + type + ", status=" + status + ", data=" + data + ", quatReal=" + quatReal + ", radianAccuracy="
				+ radianAccuracy + ", timestamp=" + timestamp + '}';
	}
}
