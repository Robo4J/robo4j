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
 * Command IDs (section 6.4, page 42 in the manual). These are used to
 * calibrate, initialize, set orientation, tare etc the sensor.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum ShtpCommandId {
    //@formatter:off
    NONE            (0),
    ERRORS          (1),
    COUNTER         (2),
    TARE            (3),
    INITIALIZE      (4),
    DCD             (6),
    ME_CALIBRATE    (7),
    DCD_PERIOD_SAVE (9),
    OSCILLATOR      (10),
    CLEAR_DCD       (11);
    //@formatter:on

    private static final Map<Integer, ShtpCommandId> map = getMap();
    private final int id;

    ShtpCommandId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ShtpCommandId getById(int id) {
        ShtpCommandId command = map.get(id);
        return command == null ? NONE : command;
    }

    private static Map<Integer, ShtpCommandId> getMap() {
        Map<Integer, ShtpCommandId> map = new HashMap<>();
        for (ShtpCommandId c : values()) {
            map.put(c.id, c);
        }
        return map;
    }
}
