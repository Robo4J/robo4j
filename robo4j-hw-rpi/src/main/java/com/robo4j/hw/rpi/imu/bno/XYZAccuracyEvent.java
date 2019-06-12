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
 * XYZAccuracyEvent BNO Accelerometer Event used for Raw, Linear Accelerometer,
 * Gyroscope, Magnetometer
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class XYZAccuracyEvent {

	public enum Type {
		ACCELEROMETER_RAW, ACCELEROMETER_LINEAR, GYROSCOPE, MAGNETOMETER;
	}

	private final Type type;
	private final float x;
	private final float y;
	private final float z;
	private final int accuracy;

	public XYZAccuracyEvent(Type type, float x, float y, float z, int accuracy) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.accuracy = accuracy;
    }

	public Type getType() {
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
	public String toString() {
		return "XYZAccuracyEvent{" + "type=" + type + ", x=" + x + ", y=" + y + ", z=" + z + ", accuracy=" + accuracy
				+ '}';
	}
}
