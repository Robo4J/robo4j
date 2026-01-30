/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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

import java.io.Serial;
import java.util.Objects;

/**
 * Event for gyro-integrated rotation vector sensor reports.
 * This high-rate rotation vector includes angular velocity data.
 * Delivered on SHTP channel 5 (GYRO).
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class GyroIntegratedRVEvent extends VectorEvent {
    @Serial
    private static final long serialVersionUID = 1L;

    private final float angularVelocityX;
    private final float angularVelocityY;
    private final float angularVelocityZ;

    public GyroIntegratedRVEvent(int status, Tuple3f quaternionIJK, long timestamp,
                                  float quatReal, float angVelX, float angVelY, float angVelZ) {
        super(DataEventType.GYRO_INTEGRATED_RV, status, quaternionIJK, timestamp, quatReal, 0);
        this.angularVelocityX = angVelX;
        this.angularVelocityY = angVelY;
        this.angularVelocityZ = angVelZ;
    }

    /**
     * Returns angular velocity around X axis in rad/s.
     *
     * @return angular velocity X
     */
    public float getAngularVelocityX() {
        return angularVelocityX;
    }

    /**
     * Returns angular velocity around Y axis in rad/s.
     *
     * @return angular velocity Y
     */
    public float getAngularVelocityY() {
        return angularVelocityY;
    }

    /**
     * Returns angular velocity around Z axis in rad/s.
     *
     * @return angular velocity Z
     */
    public float getAngularVelocityZ() {
        return angularVelocityZ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GyroIntegratedRVEvent that = (GyroIntegratedRVEvent) o;
        return Float.compare(that.angularVelocityX, angularVelocityX) == 0
                && Float.compare(that.angularVelocityY, angularVelocityY) == 0
                && Float.compare(that.angularVelocityZ, angularVelocityZ) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), angularVelocityX, angularVelocityY, angularVelocityZ);
    }

    @Override
    public String toString() {
        return "GyroIntegratedRVEvent{" +
                "quaternion=(" + getData().x + ", " + getData().y + ", " + getData().z + ", " + getQuatReal() + ")" +
                ", angVel=(" + angularVelocityX + ", " + angularVelocityY + ", " + angularVelocityZ + ")" +
                ", status=" + getStatus() +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
