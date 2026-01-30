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
package com.robo4j.hw.rpi.i2c.sparkfun;

import java.io.IOException;

/**
 * Simple hello world example for SparkFun SerLCD 20x4.
 *
 * @author Marcus Hirt (@hirt)
 */
public class HelloSerLcd {

    public static void main(String[] args) throws IOException {
        SparkFunSerLcdDevice lcd = new SparkFunSerLcdDevice();
        lcd.setContrast(50);
        lcd.setBacklight(SparkFunSerLcdDevice.Color.CYAN);
        lcd.printRow(0, "Hello World!");
        lcd.printRow(1, "--------------------");
        lcd.printRow(2, "SparkFun SerLCD 20x4");
        lcd.printRow(3, "Robo4J");
    }
}
