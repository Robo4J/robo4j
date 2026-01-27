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

import java.io.IOException;

/**
 * Repeatedly reads and displays the temperature (in C), pressure (in hPa) and
 * barometric altitude (in m). Good example to test that your BMP581 device is working.
 *
 * @author Marcus Hirt (@hirt)
 */
public class BMP581Example {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("BMP581 Sensor Example");
        System.out.println("=====================");

        BMP581Device bmp = new BMP581Device();
        System.out.println("BMP581 Chip ID: 0x" + Integer.toHexString(bmp.getChipId()));
        System.out.println("Temperature OSR: " + bmp.getTemperatureOversampling());
        System.out.println("Pressure OSR: " + bmp.getPressureOversampling());
        System.out.println("IIR Filter: " + bmp.getPressureIirFilter());
        System.out.println("Power Mode: " + bmp.getPowerMode());
        System.out.println();

        while (true) {
            float temperature = bmp.readTemperature();
            float pressure = bmp.readPressure();
            float altitude = bmp.readAltitude();

            System.out.printf("Temperature: %.2f°C, Pressure: %.2f hPa, Altitude: %.2f m%n",
                    temperature,
                    pressure / 100.0f,
                    altitude);
            Thread.sleep(2000);
        }
    }
}
