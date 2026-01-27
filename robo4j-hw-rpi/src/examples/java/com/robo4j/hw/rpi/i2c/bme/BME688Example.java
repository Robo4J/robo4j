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
 * Repeatedly reads and displays temperature (in C), pressure (in hPa), humidity (in %),
 * gas resistance (in kOhm), and barometric altitude (in m) from a BME688 sensor.
 * Good example to test that your BME688 device is working.
 *
 * @author Marcus Hirt (@hirt)
 */
public class BME688Example {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("BME688 Environmental Sensor Example");
        System.out.println("====================================");

        BME688Device bme688 = new BME688Device();
        System.out.println("BME688 sensor initialized successfully");
        System.out.println();

        System.out.println("Running self-test...");
        if (!bme688.selfTest()) {
            System.out.println("Self-test FAILED. Check sensor wiring and connections.");
            return;
        }
        System.out.println("Self-test passed.");
        System.out.println();

        int readCount = 0;
        while (true) {
            readCount++;
            System.out.println("--- Reading #" + readCount + " ---");

            // Read air quality data (includes T/P/H plus simplified IAQ metrics)
            BME688Device.AirQualityData data = bme688.readAirQuality();

            System.out.printf("Temperature: %.2f C%n", data.getTemperature());
            System.out.printf("Pressure: %.2f hPa%n", data.getPressureHPa());
            System.out.printf("Humidity: %.2f%%%n", data.getHumidity());
            System.out.printf("Gas Resistance: %.2f kOhm%n", data.getGasResistance() / 1000.0);

            // Simplified air quality estimates (for accurate values, use Bosch BSEC library)
            if (data.isValid()) {
                System.out.printf("IAQ: %.0f (%s)%n", data.getIAQ(), data.getIAQLevel().getDescription());
                System.out.printf("CO2 Equivalent: %.0f ppm%n", data.getCO2Equivalent());
            }
            System.out.println();

            Thread.sleep(3000);
        }
    }
}
