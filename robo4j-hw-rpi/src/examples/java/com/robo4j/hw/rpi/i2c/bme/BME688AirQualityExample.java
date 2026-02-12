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

import static java.lang.IO.*;

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
        println("BME688 Simplified Air Quality Example");
        println("======================================");
        println();
        println("NOTE: This example uses simplified algorithms for demonstration.");
        println("For production use, consider the Bosch BSEC library for accurate results.");
        println();

        BME688Device bme688 = new BME688Device();
        println("BME688 sensor initialized.");
        println();

        // Step 1: Choose baseline mode
        println("--- Baseline Mode Selection ---");
        println("  1) Adaptive baseline (automatic, self-calibrates over time)");
        println("  2) Explicit calibration (takes readings now in clean air)");
        println("  3) Fixed baseline (use a pre-established calibration value)");
        print("Select mode [1]: ");

        try (Scanner scanner = new Scanner(System.in)) {
            String choice = scanner.nextLine().trim();
            if ("2".equals(choice)) {
                println();
                println("Explicit calibration selected.");
                println("Make sure you are in clean air (outdoors or well-ventilated room).");
                println("Taking 10 readings to establish baseline...");
                bme688.calibrateBaseline(10);
                println("Baseline resistance: %.0f Ohms".formatted(bme688.getBaselineResistance()));
            } else if ("3".equals(choice)) {
                println();
                println("Fixed baseline selected.");
                bme688.setBaselineResistance(108981f);
                println("Baseline resistance set to: %.0f Ohms".formatted(bme688.getBaselineResistance()));
                // This is actually the baseline for the BME sensor in my lab. ;)
                println("Tip: Run BME688BaselineCalibrationExample to establish your own baseline. This is the baseline for a specific sensor.");
            } else {
                println();
                println("Adaptive baseline selected.");
                println("The baseline will self-calibrate as the sensor observes air quality changes.");
                println("Initial default baseline: %.0f Ohms".formatted(bme688.getBaselineResistance()));
            }
            println();

            // Step 2: Multi-temperature gas scan
            println("--- Multi-Temperature Gas Scan ---");
            println("Scanning at 200C, 300C, and 400C for VOC characterization...");
            GasScanResult scanResult = bme688.performStandardGasScan();
            printGasScanResult(scanResult);
            println();

            // Step 3: Continuous monitoring
            println("--- Continuous Air Quality Monitoring ---");
            println("Press Ctrl+C to stop.");
            println();

            int readCount = 0;
            float lastBaseline = bme688.getBaselineResistance();
            while (true) {
                readCount++;
                AirQualityData data = bme688.readAirQuality();

                print("Reading #%d: IAQ=%.0f (%s), CO2eq=%.0f ppm, bVOC=%.2f ppm, T=%.1fC, H=%.1f%%".formatted(
                        readCount,
                        data.getIAQ(),
                        data.getIAQLevel().getDescription(),
                        data.getCO2Equivalent(),
                        data.getBVOC(),
                        data.getTemperature(),
                        data.getHumidity()));

                // Show baseline updates in adaptive mode
                float currentBaseline = bme688.getBaselineResistance();
                if (!bme688.isBaselineCalibrated() && currentBaseline != lastBaseline) {
                    print(" [baseline: %.0f]".formatted(currentBaseline));
                    lastBaseline = currentBaseline;
                }
                println();

                if (!bme688.isBurnInComplete()) {
                    println("  (burn-in: %d readings)".formatted(
                            bme688.getReadingCount()));
                }

                Thread.sleep(3000);
            }
        }
    }

    private static void printGasScanResult(GasScanResult result) {
        println("  Gas resistance at different temperatures:");
        for (int i = 0; i < result.getProfileCount(); i++) {
            GasScanProfile profile = result.getProfile(i);
            if (result.isValid(i)) {
                println("    %dC: %.0f Ohms".formatted(profile.temperatureC(), result.getResistance(i)));
            } else {
                println("    %dC: invalid".formatted(profile.temperatureC()));
            }
        }

        // Resistance ratio can help identify gas types
        if (result.getProfileCount() >= 2) {
            float ratio = result.getResistanceRatio(0, result.getProfileCount() - 1);
            if (!Float.isNaN(ratio)) {
                println("  Low/High temp resistance ratio: %.2f".formatted(ratio));
            }
        }

        println("  Air quality from primary profile:");
        AirQualityData aq = result.getAirQuality();
        println("    IAQ: %.0f (%s)".formatted(aq.getIAQ(), aq.getIAQLevel().getDescription()));
        println("    CO2 Equivalent: %.0f ppm".formatted(aq.getCO2Equivalent()));
        println("    bVOC Estimate: %.2f ppm".formatted(aq.getBVOC()));
    }
}
