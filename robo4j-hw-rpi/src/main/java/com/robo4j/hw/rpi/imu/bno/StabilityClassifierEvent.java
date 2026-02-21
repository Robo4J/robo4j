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
 * Event for stability classifier sensor reports.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class StabilityClassifierEvent extends DataEvent3f {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Stability classification values.
     */
    public enum Classification {
        UNKNOWN(0),
        ON_TABLE(1),
        STATIONARY(2),
        STABLE(3),
        IN_MOTION(4);

        private final int id;

        Classification(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static Classification fromId(int id) {
            for (Classification c : values()) {
                if (c.id == id) {
                    return c;
                }
            }
            return UNKNOWN;
        }
    }

    private final Classification classification;

    public StabilityClassifierEvent(int status, long timestamp, int classificationId) {
        super(DataEventType.STABILITY_CLASSIFIER, status, new Tuple3f(classificationId, 0, 0), timestamp);
        this.classification = Classification.fromId(classificationId);
    }

    /**
     * Returns the stability classification.
     *
     * @return classification
     */
    public Classification getClassification() {
        return classification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StabilityClassifierEvent that = (StabilityClassifierEvent) o;
        return classification == that.classification;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), classification);
    }

    @Override
    public String toString() {
        return "StabilityClassifierEvent{" +
                "classification=" + classification +
                ", status=" + getStatus() +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
