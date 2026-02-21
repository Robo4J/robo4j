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
import java.util.Arrays;
import java.util.Objects;

/**
 * Event for personal activity classifier sensor reports.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ActivityClassifierEvent extends DataEvent3f {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Activity types recognized by the classifier.
     */
    public enum Activity {
        UNKNOWN(0),
        IN_VEHICLE(1),
        ON_BICYCLE(2),
        ON_FOOT(3),
        STILL(4),
        TILTING(5),
        WALKING(6),
        RUNNING(7);

        private final int id;

        Activity(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static Activity fromId(int id) {
            for (Activity a : values()) {
                if (a.id == id) {
                    return a;
                }
            }
            return UNKNOWN;
        }
    }

    private final Activity mostLikelyActivity;
    private final int[] confidences;

    public ActivityClassifierEvent(int status, long timestamp, int mostLikelyActivityId, int[] confidences) {
        super(DataEventType.ACTIVITY_CLASSIFIER, status, new Tuple3f(mostLikelyActivityId, 0, 0), timestamp);
        this.mostLikelyActivity = Activity.fromId(mostLikelyActivityId);
        this.confidences = confidences != null ? Arrays.copyOf(confidences, confidences.length) : new int[0];
    }

    /**
     * Returns the most likely activity.
     *
     * @return most likely activity
     */
    public Activity getMostLikelyActivity() {
        return mostLikelyActivity;
    }

    /**
     * Returns the confidence (0-100) for a specific activity.
     *
     * @param activity the activity to get confidence for
     * @return confidence percentage
     */
    public int getConfidence(Activity activity) {
        int idx = activity.getId();
        if (idx >= 0 && idx < confidences.length) {
            return confidences[idx];
        }
        return 0;
    }

    /**
     * Returns all confidence values.
     *
     * @return array of confidence values
     */
    public int[] getConfidences() {
        return Arrays.copyOf(confidences, confidences.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ActivityClassifierEvent that = (ActivityClassifierEvent) o;
        return mostLikelyActivity == that.mostLikelyActivity && Arrays.equals(confidences, that.confidences);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), mostLikelyActivity);
        result = 31 * result + Arrays.hashCode(confidences);
        return result;
    }

    @Override
    public String toString() {
        return "ActivityClassifierEvent{" +
                "mostLikelyActivity=" + mostLikelyActivity +
                ", confidences=" + Arrays.toString(confidences) +
                ", status=" + getStatus() +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
