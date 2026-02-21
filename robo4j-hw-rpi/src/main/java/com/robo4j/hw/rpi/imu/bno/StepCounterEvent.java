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
 * Event for step counter sensor reports.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class StepCounterEvent extends DataEvent3f {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int steps;
    private final long latencyMicros;

    public StepCounterEvent(int status, long timestamp, int steps, long latencyMicros) {
        super(DataEventType.STEP_COUNTER, status, new Tuple3f(steps, 0, 0), timestamp);
        this.steps = steps;
        this.latencyMicros = latencyMicros;
    }

    /**
     * Returns the total number of steps counted.
     *
     * @return step count
     */
    public int getSteps() {
        return steps;
    }

    /**
     * Returns the latency in microseconds.
     *
     * @return latency in microseconds
     */
    public long getLatencyMicros() {
        return latencyMicros;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StepCounterEvent that = (StepCounterEvent) o;
        return steps == that.steps && latencyMicros == that.latencyMicros;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), steps, latencyMicros);
    }

    @Override
    public String toString() {
        return "StepCounterEvent{" +
                "steps=" + steps +
                ", latencyMicros=" + latencyMicros +
                ", status=" + getStatus() +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
