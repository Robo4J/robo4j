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
package com.robo4j.hw.rpi.i2c.magnetometer;

import com.robo4j.hw.rpi.i2c.magnetometer.MagnetometerLSM303Device.Mode;
import com.robo4j.hw.rpi.i2c.magnetometer.MagnetometerLSM303Device.Rate;
import com.robo4j.hw.rpi.utils.I2cBus;
import com.robo4j.math.geometry.Matrix3f;
import com.robo4j.math.geometry.Tuple3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Magnetometer example for trying out a calibrated magnetometer.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CalibratedMagnetometerExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalibratedMagnetometerExample.class);
    private static final int TIMEOUT_SEC = 1;
    private final static ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1);

    private static class MagnetometerPoller implements Runnable {
        private final MagnetometerLSM303Device magDevice;

        public MagnetometerPoller(MagnetometerLSM303Device magDevice) {
            this.magDevice = magDevice;
        }

        @Override
        public void run() {
            try {
                Tuple3f magResult = magDevice.read();
                LOGGER.info("Heading:{}", MagnetometerLSM303Device.getCompassHeading(magResult));
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // NOTE(Marcus/Jul 23, 2017): Change the actual bias vector and
        // transform matrix to match your calibration values.
        // See the MagViz tool for more information.
        Tuple3f bias = new Tuple3f(-44.689f, -2.0665f, -15.240f);
        Matrix3f transform = new Matrix3f(1.887f, 5.987f, -5.709f, 5.987f, 1.528f, -2.960f, -5.709f, -2.960f, 9.761f);
        MagnetometerLSM303Device magnetometer = new MagnetometerLSM303Device(I2cBus.BUS_1, 0x1e, Mode.CONTINUOUS_CONVERSION, Rate.RATE_7_5,
                false, bias, transform);
        LOGGER.info(
                "Starting to read and print the heading. Make sure the magnetometer is flat in the XY-plane (this example does not do tilt compensated heading).");
        LOGGER.info("Press <Enter> to quit...");
        EXECUTOR.scheduleAtFixedRate(new MagnetometerPoller(magnetometer), 100, 500, TimeUnit.MILLISECONDS);
        System.in.read();
        EXECUTOR.shutdown();
        var terminated = EXECUTOR.awaitTermination(TIMEOUT_SEC, TimeUnit.SECONDS);
        if(!terminated){
            LOGGER.warn("example not terminated properly.");
            EXECUTOR.shutdownNow();
        }
        LOGGER.info("Goodbye!");
    }
}
