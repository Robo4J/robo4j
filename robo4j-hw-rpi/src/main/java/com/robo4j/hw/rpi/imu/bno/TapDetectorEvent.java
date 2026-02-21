/*
 * Copyright (c) 2026, Marcus Hirt, Miroslav Wengner
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
 * Event for tap detector sensor reports.
 * See SH-2 Reference Manual for tap flag bit definitions.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class TapDetectorEvent extends DataEvent3f {
    @Serial
    private static final long serialVersionUID = 1L;

    // Tap flag bit definitions
    public static final int TAP_X = 0x01;
    public static final int TAP_X_POSITIVE = 0x02;
    public static final int TAP_Y = 0x04;
    public static final int TAP_Y_POSITIVE = 0x08;
    public static final int TAP_Z = 0x10;
    public static final int TAP_Z_POSITIVE = 0x20;
    public static final int TAP_DOUBLE = 0x40;

    private final int tapFlags;

    public TapDetectorEvent(int status, long timestamp, int tapFlags) {
        super(DataEventType.TAP_DETECTOR, status, new Tuple3f(tapFlags, 0, 0), timestamp);
        this.tapFlags = tapFlags;
    }

    /**
     * Returns the raw tap flags.
     *
     * @return tap flags
     */
    public int getTapFlags() {
        return tapFlags;
    }

    public boolean isTapX() {
        return (tapFlags & TAP_X) != 0;
    }

    public boolean isTapXPositive() {
        return (tapFlags & TAP_X_POSITIVE) != 0;
    }

    public boolean isTapY() {
        return (tapFlags & TAP_Y) != 0;
    }

    public boolean isTapYPositive() {
        return (tapFlags & TAP_Y_POSITIVE) != 0;
    }

    public boolean isTapZ() {
        return (tapFlags & TAP_Z) != 0;
    }

    public boolean isTapZPositive() {
        return (tapFlags & TAP_Z_POSITIVE) != 0;
    }

    public boolean isDoubleTap() {
        return (tapFlags & TAP_DOUBLE) != 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TapDetectorEvent that = (TapDetectorEvent) o;
        return tapFlags == that.tapFlags;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tapFlags);
    }

    @Override
    public String toString() {
        return "TapDetectorEvent{" +
                "tapFlags=0x" + Integer.toHexString(tapFlags) +
                ", doubleTap=" + isDoubleTap() +
                ", status=" + getStatus() +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
