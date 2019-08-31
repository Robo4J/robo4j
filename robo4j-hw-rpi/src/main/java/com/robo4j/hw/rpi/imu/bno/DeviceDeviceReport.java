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

import java.util.HashMap;
import java.util.Map;

/**
 * All the ways we can configure or talk to the BNO080, figure 34, page 36
 * reference manual These are used for low level communication with the sensor,
 * on DeviceChannel 2 (CONTROL)
 */
public enum DeviceDeviceReport implements DeviceReport {
    //@formatter:off
    NONE                                    (-1),
    ADVERTISEMENT                           (0x00),
    COMMAND_RESPONSE                        (0xF1),
    COMMAND_REQUEST                         (0xF2),
    FRS_READ_RESPONSE                       (0xF3),
    FRS_READ_REQUEST                        (0xF4),
    PRODUCT_ID_RESPONSE                     (0xF8),
    PRODUCT_ID_REQUEST                      (0xF9),
    BASE_TIMESTAMP                          (0xFB),
    SET_FEATURE_COMMAND                     (0xFD),
    GET_FEATURE_REQUEST                     (0xFE),
    GET_FEATURE_RESPONSE                    (0xFC),
    FORCE_SENSOR_FLUSH                      (0xF0),
    FLUSH_COMPLETED                         (0xEF);
    //@formatter:on

    private final static Map<Integer, DeviceDeviceReport> map = getMap();
    private final int id;
    private final DeviceChannel deviceChannel = DeviceChannel.CONTROL;

    DeviceDeviceReport(int id) {
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

    public static DeviceDeviceReport getById(int id) {
        DeviceDeviceReport report = map.get(id);
        return report == null ? NONE : report;
    }

    private static Map<Integer, DeviceDeviceReport> getMap() {
        Map<Integer, DeviceDeviceReport> map = new HashMap<>();
        for (DeviceDeviceReport r : values()) {
            map.put(r.getId(), r);
        }
        return map;
    }
}
