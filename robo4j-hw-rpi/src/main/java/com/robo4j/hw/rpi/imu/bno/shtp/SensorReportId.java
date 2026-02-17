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

package com.robo4j.hw.rpi.imu.bno.shtp;

import java.util.HashMap;
import java.util.Map;

/**
 * Sensor reports received on ShtpChannel 3
 */
public enum SensorReportId implements ShtpReportIds {
    //@formatter:off
    NONE                            (-1),
    ACCELEROMETER                   (0x01),
    GYROSCOPE                       (0x02),
    MAGNETIC_FIELD                  (0x03),
    LINEAR_ACCELERATION             (0x04),
    ROTATION_VECTOR                 (0x05),
    GRAVITY                         (0x06),
    GYRO_UNCALIBRATED               (0x07),
    GAME_ROTATION_VECTOR            (0x08),
    GEOMAGNETIC_ROTATION_VECTOR     (0x09),
    PRESSURE                        (0x0A),
    AMBIENT_LIGHT                   (0x0B),
    HUMIDITY                        (0x0C),
    PROXIMITY                       (0x0D),
    TEMPERATURE                     (0x0E),
    MAGNETIC_FIELD_UNCALIBRATED     (0x0F),
    TAP_DETECTOR                    (0x10),
    STEP_COUNTER                    (0x11),
    SIGNIFICANT_MOTION              (0x12),
    STABILITY_CLASSIFIER            (0x13),
    RAW_ACCELEROMETER               (0x14),
    RAW_GYROSCOPE                   (0x15),
    RAW_MAGNETOMETER                (0x16),
    STEP_DETECTOR                   (0x18),
    SHAKE_DETECTOR                  (0x19),
    TILT_DETECTOR                   (0x20),
    POCKET_DETECTOR                 (0x21),
    CIRCLE_DETECTOR                 (0x22),
    HEART_RATE_MONITOR              (0x23),
    ARVR_STAB_ROTATION_VECTOR       (0x28),
    ARVR_STAB_GAME_ROTATION_VECTOR  (0x29),
    FLIP_DETECTOR                   (0x1A),
    PICKUP_DETECTOR                 (0x1B),
    STABILITY_DETECTOR              (0x1C),
    PERSONAL_ACTIVITY_CLASSIFIER    (0x1E),
    SLEEP_DETECTOR                  (0x1F),
    GYRO_INT_ROTATION_VECTOR        (0x2A),
    IZRO_MOTION_REQUEST             (0x2B),
    BASE_TIMESTAMP                  (0xFB);
    //@formatter:on

    private static final Map<Integer, SensorReportId> map = getMap();
    private final int id;
    private final ShtpChannel shtpChannel = ShtpChannel.REPORTS;

    SensorReportId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public ShtpChannel getChannel() {
        return shtpChannel;
    }

    public static SensorReportId getById(int code) {
        SensorReportId report = map.get(code & 0xFF);
        return report == null ? NONE : report;
    }

    private static Map<Integer, SensorReportId> getMap() {
        Map<Integer, SensorReportId> map = new HashMap<>();
        for (SensorReportId r : values()) {
            map.put(r.getId(), r);
        }
        return map;
    }
}
