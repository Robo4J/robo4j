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

import com.robo4j.hw.rpi.i2c.bme.BME688Device.AirQualityData;
import com.robo4j.hw.rpi.i2c.bme.BME688Device.GasScanProfile;
import com.robo4j.hw.rpi.i2c.bme.BME688Device.GasScanResult;

import java.io.IOException;
import java.util.Scanner;

/**
 * Example demonstrating the BME688 simplified air quality monitoring features.
 * <p>
 * <strong>Important Note:</strong> This example uses simplified algorithms for IAQ,
 * CO2 equivalent, and bVOC estimation. These are approximations suitable for learning,
 * experimentation, and detecting relative air quality changes. For production applications
 * requiring accurate air quality measurements, use the Bosch BSEC (Bosch Software
 * Environmental Cluster) library instead.
 *
 * @author Marcus Hirt (@hirt)
 * @see <a href="https://www.bosch-sensortec.com/software-tools/software/bsec/">Bosch BSEC Library</a>
 */
public class BME688AirQualityExample {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("BME688 Simplified Air Quality Example");
        System.out.println("======================================");
        System.out.println();
        System.out.println("NOTE: This example uses simplified algorithms for demonstration.");
        System.out.println("For production use, consider the Bosch BSEC library for accurate results.");
        System.out.println();

        BME688Device bme688 = new BME688Device();
        System.out.println("BME688 sensor initialized.");
        System.out.println();

        // Step 1: Choose baseline mode
        System.out.println("--- Baseline Mode Selection ---");
        System.out.println("  1) Adaptive baseline (automatic, self-calibrates over time)");
        System.out.println("  2) Explicit calibration (takes readings now in clean air)");
        System.out.println("  3) Fixed baseline (use a pre-established calibration value)");
        System.out.print("Select mode [1]: ");

        try (Scanner scanner = new Scanner(System.in)) {
            String choice = scanner.nextLine().trim();
            if ("2".equals(choice)) {
                System.out.println();
                System.out.println("Explicit calibration selected.");
                System.out.println("Make sure you are in clean air (outdoors or well-ventilated room).");
                System.out.println("Taking 10 readings to establish baseline...");
                bme688.calibrateBaseline(10);
                System.out.printf("Baseline resistance: %.0f Ohms%n", bme688.getBaselineResistance());
            } else if ("3".equals(choice)) {
                System.out.println();
                System.out.println("Fixed baseline selected.");
                bme688.setBaselineResistance(108981f);
                System.out.printf("Baseline resistance set to: %.0f Ohms%n", bme688.getBaselineResistance());
                // This is actually the baseline for the BME sensor in my lab. ;) 
                System.out.println("Tip: Run BME688BaselineCalibrationExample to establish your own baseline. This is the baseline for a specific sensor.");
            } else {
                System.out.println();
                System.out.println("Adaptive baseline selected.");
                System.out.println("The baseline will self-calibrate as the sensor observes air quality changes.");
                System.out.printf("Initial default baseline: %.0f Ohms%n", bme688.getBaselineResistance());
            }
            System.out.println();

            // Step 2: Multi-temperature gas scan
            System.out.println("--- Multi-Temperature Gas Scan ---");
            System.out.println("Scanning at 200C, 300C, and 400C for VOC characterization...");
            GasScanResult scanResult = bme688.performStandardGasScan();
            printGasScanResult(scanResult);
            System.out.println();

            // Step 3: Continuous monitoring
            System.out.println("--- Continuous Air Quality Monitoring ---");
            System.out.println("Press Ctrl+C to stop.");
            System.out.println();

            int readCount = 0;
            float lastBaseline = bme688.getBaselineResistance();
            while (true) {
                readCount++;
                AirQualityData data = bme688.readAirQuality();

                System.out.printf("Reading #%d: IAQ=%.0f (%s), CO2eq=%.0f ppm, bVOC=%.2f ppm, T=%.1fC, H=%.1f%%",
                        readCount,
                        data.getIAQ(),
                        data.getIAQLevel().getDescription(),
                        data.getCO2Equivalent(),
                        data.getBVOC(),
                        data.getTemperature(),
                        data.getHumidity());

                // Show baseline updates in adaptive mode
                float currentBaseline = bme688.getBaselineResistance();
                if (!bme688.isBaselineCalibrated() && currentBaseline != lastBaseline) {
                    System.out.printf(" [baseline: %.0f]", currentBaseline);
                    lastBaseline = currentBaseline;
                }
                System.out.println();

                if (!bme688.isBurnInComplete()) {
                    System.out.printf("  (burn-in: %d readings)%n",
                            bme688.getReadingCount());
                }

                Thread.sleep(3000);
            }
        }
    }

    private static void printGasScanResult(GasScanResult result) {
        System.out.println("  Gas resistance at different temperatures:");
        for (int i = 0; i < result.getProfileCount(); i++) {
            GasScanProfile profile = result.getProfile(i);
            if (result.isValid(i)) {
                System.out.printf("    %dC: %.0f Ohms%n", profile.temperatureC(), result.getResistance(i));
            } else {
                System.out.printf("    %dC: invalid%n", profile.temperatureC());
            }
        }

        // Resistance ratio can help identify gas types
        if (result.getProfileCount() >= 2) {
            float ratio = result.getResistanceRatio(0, result.getProfileCount() - 1);
            if (!Float.isNaN(ratio)) {
                System.out.printf("  Low/High temp resistance ratio: %.2f%n", ratio);
            }
        }

        System.out.println("  Air quality from primary profile:");
        AirQualityData aq = result.getAirQuality();
        System.out.printf("    IAQ: %.0f (%s)%n", aq.getIAQ(), aq.getIAQLevel().getDescription());
        System.out.printf("    CO2 Equivalent: %.0f ppm%n", aq.getCO2Equivalent());
        System.out.printf("    bVOC Estimate: %.2f ppm%n", aq.getBVOC());
    }
}
