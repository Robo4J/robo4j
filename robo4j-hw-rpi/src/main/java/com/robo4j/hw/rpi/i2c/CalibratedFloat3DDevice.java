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
package com.robo4j.hw.rpi.i2c;

import com.robo4j.math.geometry.Tuple3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Wrapper class for readable devices returning Float3D, allowing for calibration.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CalibratedFloat3DDevice implements ReadableDevice<Tuple3f> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalibratedFloat3DDevice.class);
    private final Tuple3f centerOffsets;
    private final Tuple3f rangeMultipliers;
    private final ReadableDevice<Tuple3f> device;

    public CalibratedFloat3DDevice(ReadableDevice<Tuple3f> device, Tuple3f offsets, Tuple3f multipliers) {
        this.device = device;
        this.centerOffsets = offsets;
        this.rangeMultipliers = multipliers;
    }

    public Tuple3f read() throws IOException {
        Tuple3f value = device.read();
        value.add(centerOffsets);
        value.multiply(rangeMultipliers);
        return value;
    }

    public void setCalibration(Tuple3f offsets, Tuple3f multipliers) {
        centerOffsets.set(offsets);
        rangeMultipliers.set(multipliers);
        LOGGER.debug("Gyro offsets: {}", centerOffsets);
        LOGGER.debug("Gyro multipliers: {}", rangeMultipliers);
    }

    protected Tuple3f getRangeMultipliers() {
        return rangeMultipliers;
    }

    protected Tuple3f getCenterOffsets() {
        return centerOffsets;
    }
}
