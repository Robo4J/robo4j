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

import com.robo4j.hw.rpi.i2c.bme.BME688Device.Oversampling;

import java.io.IOException;

import static java.lang.IO.*;

/**
 * Example demonstrating different oversampling configurations for the BME688 sensor.
 * Shows how oversampling affects measurement precision and noise reduction.
 *
 * Higher oversampling:
 * - Reduces noise in measurements
 * - Increases measurement time
 * - Increases power consumption
 *
 * @author Marcus Hirt (@hirt)
 */
public class BME688OversamplingExample {

    public static void main(String[] args) throws IOException, InterruptedException {
        println("BME688 Oversampling Configuration Example");
        println("==========================================");
        println();

        // Configuration 1: Ultra-low power (1x oversampling for all)
        println("--- Ultra-Low Power Mode (1x oversampling) ---");
        testConfiguration(Oversampling.X1, Oversampling.X1, Oversampling.X1, "Ultra-Low Power");

        // Configuration 2: Standard mode (2x T, 4x P, 2x H)
        println("--- Standard Mode (2x/4x/2x oversampling) ---");
        testConfiguration(Oversampling.X2, Oversampling.X4, Oversampling.X2, "Standard");

        // Configuration 3: High resolution mode (16x oversampling for all)
        println("--- High Resolution Mode (16x oversampling) ---");
        testConfiguration(Oversampling.X16, Oversampling.X16, Oversampling.X16, "High Resolution");

        // Configuration 4: Weather monitoring (1x T, 1x P, skip H)
        println("--- Weather Monitoring Mode (no humidity) ---");
        testConfiguration(Oversampling.X1, Oversampling.X1, Oversampling.SKIP, "Weather Monitoring");

        println("Example completed.");
    }

    private static void testConfiguration(Oversampling tempOs, Oversampling pressOs,
                                          Oversampling humOs, String modeName)
            throws IOException, InterruptedException {

        BME688Device bme688 = new BME688Device(tempOs, pressOs, humOs);

        println("Configuration: %s - T:%dx, P:%dx, H:%dx".formatted(
                modeName, tempOs.getSamples(), pressOs.getSamples(), humOs.getSamples()));

        // Take 5 readings to observe stability
        for (int i = 1; i <= 5; i++) {
            BME688Device.SensorData data = bme688.readAll();
            println("  Reading %d: T=%.2f C, P=%.2f hPa, H=%.2f%%".formatted(
                    i, data.getTemperature(), data.getPressureHPa(), data.getHumidity()));
            Thread.sleep(500);
        }
        println();
    }
}
