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
public class VectorEvent extends DataEvent3f {
	private static final long serialVersionUID = 1L;
	private static final EnumSet<DataEventType> ALLOWED = EnumSet.of(DataEventType.VECTOR_ROTATION, DataEventType.VECTOR_GAME);

	private final float quatReal;
	private final float radianAccuracy;

	public VectorEvent(DataEventType type, int status, Tuple3f data, long timestamp, float quatReal, float radianAccuracy) {
		super(type, status, data, timestamp);
		if (ALLOWED.contains(type)) {
			this.quatReal = quatReal;
			this.radianAccuracy = radianAccuracy;
		} else {
			this.quatReal = 0;
			this.radianAccuracy = 0;
		}
	}

	public float getQuatReal() {
		return quatReal;
	}

	public float getRadianAccuracy() {
		return radianAccuracy;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		VectorEvent that = (VectorEvent) o;
		return super.equals(o) && Float.compare(that.quatReal, quatReal) == 0 && Float.compare(that.radianAccuracy, radianAccuracy) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getType(), getStatus(), getData(), quatReal, radianAccuracy, getTimestamp());
	}

	@Override
	public String toString() {
		return "VectorEvent{" + "type=" + getType() + ", status=" + getStatus() + ", data=" + getData() + ", quatReal=" + quatReal
				+ ", radianAccuracy=" + radianAccuracy + ", timestamp=" + getTimestamp() + '}';
	}
}
