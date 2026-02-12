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
package com.robo4j.hw.rpi.i2c.bmp;

import com.robo4j.hw.rpi.i2c.bmp.BMP581Device.IirFilter;
import com.robo4j.hw.rpi.i2c.bmp.BMP581Device.Oversampling;

import java.io.IOException;

import static java.lang.IO.*;

/**
 * Interactive demonstration of the BMP581 pressure sensor precision.
 * Establishes a baseline measurement, then shows real-time delta changes
 * in pressure (hPa) and altitude (cm) as the sensor is moved.
 *
 * @author Marcus Hirt (@hirt)
 */
public class BMP581OversamplingExample {
    private static final int BASELINE_SAMPLES = 20;

    // Approximate conversion: 1 hPa ≈ 8.3 meters altitude change at sea level
    private static final float HPA_TO_CM = 830.0f;

    public static void main(String[] args) throws IOException, InterruptedException {
        println("BMP581 Pressure Sensor Demo");
        println("===========================");
        println();

        BMP581Device bmp = new BMP581Device();
        println("BMP581 Chip ID: 0x" + Integer.toHexString(bmp.getChipId()));

        // Use high oversampling for best accuracy
        bmp.setOversampling(Oversampling.OSR_128X, Oversampling.OSR_128X);
        // Enable IIR low-pass filter to reduce short-term noise
        bmp.setIirFilter(IirFilter.COEFF_2, IirFilter.COEFF_2);
        println("Oversampling: " + bmp.getPressureOversampling());
        println("IIR Filter: " + bmp.getPressureIirFilter());
        println();

        // Wait for user to position sensor
        readln("Place the sensor at baseline position (e.g., on desk). Press Enter when ready...");
        println();

        // Measure baseline
        println("Measuring baseline... do not move the sensor.");
        float baselinePressure = 0;
        for (int i = 0; i < BASELINE_SAMPLES; i++) {
            baselinePressure += bmp.readPressure();
            print(".");
            Thread.sleep(50);
        }
        baselinePressure /= BASELINE_SAMPLES;
        println();
        println("Baseline: %.2f hPa".formatted(baselinePressure / 100.0f));
        println();

        println("Now move the sensor around to see delta change in pressure (and approximate altitude).");
        println("Press Ctrl+C to exit.");
        println();

        while (true) {
            float pressure = bmp.readPressure();
            float deltaHpa = (pressure - baselinePressure) / 100.0f;
            float deltaCm = deltaHpa * HPA_TO_CM;

            print("\rPressure: %.2f hPa | Delta: %+.3f hPa | ~Altitude: %+.1f cm   ".formatted(
                    pressure / 100.0f, deltaHpa, -deltaCm));

            Thread.sleep(100);
        }
    }
}
