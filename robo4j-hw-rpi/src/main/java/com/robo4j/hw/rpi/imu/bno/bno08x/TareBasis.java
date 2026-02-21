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

package com.robo4j.hw.rpi.imu.bno.bno08x;

/**
 * Specifies which rotation vector to use as the basis for tare adjustment.
 * See SH-2 Reference Manual section 6.4.3.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum TareBasis {
    ROTATION_VECTOR(0),
    GAME_ROTATION_VECTOR(1),
    GEOMAGNETIC_ROTATION_VECTOR(2),
    GYRO_INTEGRATED_ROTATION_VECTOR(3),
    ARVR_STABILIZED_ROTATION_VECTOR(4),
    ARVR_STABILIZED_GAME_ROTATION_VECTOR(5);

    private final int id;

    TareBasis(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
