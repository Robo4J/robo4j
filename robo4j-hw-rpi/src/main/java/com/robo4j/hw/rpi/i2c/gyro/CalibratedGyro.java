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
package com.robo4j.hw.rpi.i2c.gyro;

import com.robo4j.hw.rpi.i2c.CalibratedFloat3DDevice;
import com.robo4j.hw.rpi.i2c.ReadableDevice;
import com.robo4j.math.geometry.Tuple3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

public class CalibratedGyro extends CalibratedFloat3DDevice {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalibratedGyro.class);
    private static final int NUMBER_OF_CALIBRATION_READINGS = 40;
    private static final int NUMBER_OF_CALIBRATION_READINGS_TO_DROP = 5;
    private static final Tuple3f RANGE_MULTIPLIERS = new Tuple3f(1, 1, -1);

    public CalibratedGyro(ReadableDevice<Tuple3f> device) {
        super(device, new Tuple3f(), RANGE_MULTIPLIERS.copy());
    }

    public CalibratedGyro(ReadableDevice<Tuple3f> device, Tuple3f offsets, Tuple3f multipliers) {
        super(device, offsets, multipliers);
    }

    /**
     * Does calibration.
     *
     * @throws IOException exception
     */
    public void calibrate() throws IOException {
        setCalibration(new Tuple3f(), Tuple3f.createIdentity());
        float[] xValues = new float[NUMBER_OF_CALIBRATION_READINGS];
        float[] yValues = new float[NUMBER_OF_CALIBRATION_READINGS];
        float[] zValues = new float[NUMBER_OF_CALIBRATION_READINGS];

        for (int i = 0; i < NUMBER_OF_CALIBRATION_READINGS; i++) {
            Tuple3f tmp = read();
            xValues[i] = tmp.x;
            yValues[i] = tmp.y;
            zValues[i] = tmp.z;
            sleep(20);
        }
        Tuple3f calibration = new Tuple3f();
        calibration.x = calibrate(xValues, NUMBER_OF_CALIBRATION_READINGS_TO_DROP);
        calibration.y = calibrate(yValues, NUMBER_OF_CALIBRATION_READINGS_TO_DROP);
        calibration.z = calibrate(zValues, NUMBER_OF_CALIBRATION_READINGS_TO_DROP);
        LOGGER.info("calibrate:{}", RANGE_MULTIPLIERS);
        setCalibration(calibration, RANGE_MULTIPLIERS);
    }

    /**
     * Simple calibration function. Drop the n highest and lowest, next take the
     * average of what is left.
     */
    private float calibrate(float[] values, int drop) {
        Arrays.sort(values);
        double calibrationSum = 0;
        for (int i = drop; i < values.length - drop; i++) {
            calibrationSum += values[i];
        }
        return (float) -calibrationSum / (values.length - 2 * drop);
    }

    // TODO : review usage
    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
