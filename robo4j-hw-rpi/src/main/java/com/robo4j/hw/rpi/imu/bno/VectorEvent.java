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
 * VectorEvent used for Rotation, Game vector event
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class VectorEvent implements DeviceEvent {

	private static final EnumSet<DeviceEventType> ALLOWED = EnumSet.of(DeviceEventType.VECTOR_ROTATION, DeviceEventType.VECTOR_GAME);
	private final DeviceEventType type;
	private final int quatAccuracy;
	private final float quatI;
	private final float quatJ;
	private final float quatK;
	private final float quatReal;
	private final float radianAccuracy;
	private final long timestamp;

	public VectorEvent(DeviceEventType type, int quatAccuracy,   float quatI, float quatJ, float quatK, float quatReal,
			float radianAccuracy, long timestamp) {
		if (ALLOWED.contains(type)) {
		    this.quatAccuracy = quatAccuracy;
			this.type = type;
			this.quatI = quatI;
			this.quatJ = quatJ;
			this.quatK = quatK;
			this.quatReal = quatReal;
			this.radianAccuracy = radianAccuracy;
			this.timestamp = timestamp;
		} else {
			this.type = DeviceEventType.NONE;
			this.quatAccuracy = 0;
			this.quatI = 0;
			this.quatJ = 0;
			this.quatK = 0;
			this.quatReal = 0;
			this.radianAccuracy = 0;
			this.timestamp = 0;
		}

	}

	public DeviceEventType getType() {
		return type;
	}

    public int getQuatAccuracy() {
        return quatAccuracy;
    }

    public float getQuatI() {
		return quatI;
	}

	public float getQuatJ() {
		return quatJ;
	}

	public float getQuatK() {
		return quatK;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VectorEvent that = (VectorEvent) o;
        return quatAccuracy == that.quatAccuracy &&
                Float.compare(that.quatI, quatI) == 0 &&
                Float.compare(that.quatJ, quatJ) == 0 &&
                Float.compare(that.quatK, quatK) == 0 &&
                Float.compare(that.quatReal, quatReal) == 0 &&
                Float.compare(that.radianAccuracy, radianAccuracy) == 0 &&
                timestamp == that.timestamp &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, quatAccuracy, quatI, quatJ, quatK, quatReal, radianAccuracy, timestamp);
    }

    @Override
    public String toString() {
        return "VectorEvent{" +
                "type=" + type +
                ", quatAccuracy=" + quatAccuracy +
                ", quatI=" + quatI +
                ", quatJ=" + quatJ +
                ", quatK=" + quatK +
                ", quatReal=" + quatReal +
                ", radianAccuracy=" + radianAccuracy +
                ", timestamp=" + timestamp +
                '}';
    }
}
