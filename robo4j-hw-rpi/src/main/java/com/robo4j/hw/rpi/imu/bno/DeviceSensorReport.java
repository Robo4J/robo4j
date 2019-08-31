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
 * Sensor reports received on ShtpChannel 3
 */
public enum DeviceSensorReport implements DeviceReport {
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
    GYRO_INT_ROTATION_VECTOR        (0x2A),
    PRESSURE                        (0x0A),
    AMBIENT_LIGHT                   (0x0B),
    HUMIDITY                        (0x0C),
    PROXIMITY                       (0x0D),
    TEMPERATURE                     (0x0E),
    BASE_TIMESTAMP                  (0xFB);
    //@formatter:on

    private final int id;
    private final DeviceChannel deviceChannel = DeviceChannel.REPORTS;

    DeviceSensorReport(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public DeviceChannel getChannel() {
        return deviceChannel;
    }

    public static DeviceSensorReport getById(int code) {
        for (DeviceSensorReport r : values()) {
            if ((code & 0xFF) == r.getId()) {
                return r;
            }
        }
        return NONE;
    }
}
