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
package com.robo4j.hw.rpi.i2c.bme;

import java.io.IOException;

/**
 * Establishes a stable baseline for the BME688 gas sensor and prints it out
 * for use in subsequent runs. Run this in clean air (outdoors or a well-ventilated room)
 * and let it run for at least 30 minutes to allow the gas heater to fully stabilize.
 * <p>
 * The printed baseline value can be passed to {@link BME688Device#setBaselineResistance(float)}
 * on future cold starts to skip the adaptive burn-in period.
 * <p>
 * The baseline is a humidity-compensated gas resistance value and is specific to your
 * individual sensor. It remains stable across cold starts but will drift gradually
 * over weeks/months due to sensor aging. Re-run this calibration periodically
 * (e.g., monthly) for best results.
 *
 * @author Marcus Hirt (@hirt)
 */
public class BME688BaselineCalibrationExample {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("BME688 Baseline Calibration");
        System.out.println("==========================");
        System.out.println();
        System.out.println("IMPORTANT: Run this in CLEAN AIR (outdoors or well-ventilated room).");
        System.out.println("The sensor needs at least 30 minutes to fully stabilize.");
        System.out.println();

        BME688Device bme688 = new BME688Device();
        System.out.println("BME688 sensor initialized. Starting calibration...");
        System.out.println();

        // Take readings for 30 minutes, letting the adaptive baseline stabilize.
        // The MOX gas sensor heater needs significant time to reach thermal equilibrium.
        // The adaptive baseline tracks the highest compensated gas resistance,
        // which represents the cleanest air observed.
        int totalReadings = 600; // ~30 minutes at 3s intervals
        float lastBaseline = bme688.getBaselineResistance();

        for (int i = 1; i <= totalReadings; i++) {
            BME688Device.AirQualityData data = bme688.readAirQuality();
            float currentBaseline = bme688.getBaselineResistance();

            System.out.printf("  [%3d/%d] R=%.0f Ohms, T=%.1fC, H=%.1f%%, baseline=%.0f Ohms",
                    i, totalReadings,
                    data.getGasResistance(),
                    data.getTemperature(),
                    data.getHumidity(),
                    currentBaseline);

            if (currentBaseline != lastBaseline) {
                System.out.print(" *");
                lastBaseline = currentBaseline;
            }
            System.out.println();

            Thread.sleep(3000);
        }

        // Take 10 more explicit calibration readings and average them
        System.out.println();
        System.out.println("Taking 10 final calibration readings...");
        bme688.calibrateBaseline(10);

        float baseline = bme688.getBaselineResistance();
        System.out.println();
        System.out.println("============================================");
        System.out.printf("  BASELINE: %.0f%n", baseline);
        System.out.println("============================================");
        System.out.println();
        System.out.println("To use this baseline in your application:");
        System.out.println();
        System.out.println("  BME688Device bme688 = new BME688Device();");
        System.out.printf("  bme688.setBaselineResistance(%.0ff);%n", baseline);
        System.out.println();
        System.out.println("This skips the adaptive burn-in and gives immediate IAQ readings.");
        System.out.println("Re-run this calibration monthly to account for sensor aging.");
    }
}
