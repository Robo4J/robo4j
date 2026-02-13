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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.lang.IO.*;

/**
 * Demo for the SparkFun SerLCD 20x4 RGB Backlight display.
 * <p>
 * Demonstrates text display, RGB backlight colors, scrolling, and cursor control.
 *
 * @author Marcus Hirt (@hirt)
 */
public class SerLcdDemo {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void main(String[] args) throws IOException, InterruptedException {
        println("SparkFun SerLCD 20x4 Demo");
        println("=========================");

        SparkFunSerLcdDevice lcd = new SparkFunSerLcdDevice();
        lcd.setContrast(50);

        lcd.clear();
        lcd.setBacklight(SparkFunSerLcdDevice.Color.CYAN);
        lcd.printRow(0, "SparkFun SerLCD");
        lcd.printRow(1, "20x4 RGB Display");
        lcd.printRow(2, "--------------------");
        lcd.printRow(3, "Robo4J Demo");
        Thread.sleep(2000);

        println("Cycling through colors...");
        for (SparkFunSerLcdDevice.Color color : SparkFunSerLcdDevice.Color.values()) {
            if (color == SparkFunSerLcdDevice.Color.OFF) continue;
            lcd.printRow(2, "Color: " + color.name());
            lcd.setBacklight(color);
            Thread.sleep(800);
        }

        println("Custom RGB colors...");
        lcd.printRow(2, "Custom RGB:");
        int[][] customColors = {
            {255, 64, 0},    // Saturated orange
            {0, 255, 64},    // Saturated spring green
            {64, 0, 255},    // Saturated violet
            {255, 0, 128},   // Saturated rose
            {0, 255, 255},   // Saturated cyan
            {255, 255, 0},   // Saturated yellow
        };
        for (int[] rgb : customColors) {
            lcd.printRow(3, String.format("R:%d G:%d B:%d", rgb[0], rgb[1], rgb[2]));
            lcd.setBacklight(rgb[0], rgb[1], rgb[2]);
            Thread.sleep(800);
        }

        println("Scroll demo...");
        lcd.setBacklight(SparkFunSerLcdDevice.Color.WHITE);
        lcd.clear();
        lcd.printRow(0, "<<< Scroll Demo >>>");
        lcd.printRow(1, "   Left and Right   ");
        lcd.printRow(2, "====================");
        lcd.printRow(3, "  Watch me move!    ");
        Thread.sleep(1000);

        for (int i = 0; i < 8; i++) {
            lcd.scrollLeft();
            Thread.sleep(200);
        }
        Thread.sleep(500);

        for (int i = 0; i < 16; i++) {
            lcd.scrollRight();
            Thread.sleep(200);
        }
        Thread.sleep(500);

        for (int i = 0; i < 8; i++) {
            lcd.scrollLeft();
            Thread.sleep(200);
        }
        Thread.sleep(1000);

        println("Marquee demo...");
        lcd.clear();
        lcd.setBacklight(SparkFunSerLcdDevice.Color.YELLOW);
        lcd.printRow(0, "--- Marquee Demo ---");
        String marqueeText = "    Welcome to Robo4J! The Java robotics framework...    ";
        for (int i = 0; i < marqueeText.length() - 20; i++) {
            lcd.setCursor(0, 2);
            lcd.print(marqueeText.substring(i, i + 20));
            Thread.sleep(150);
        }
        Thread.sleep(1000);

        println("Showing clock for 5 seconds...");
        lcd.setBacklight(SparkFunSerLcdDevice.Color.CYAN);
        lcd.clear();
        lcd.printRow(0, "   Robo4J Clock");
        lcd.printRow(1, "--------------------");

        for (int i = 0; i < 5; i++) {
            String time = LocalDateTime.now().format(TIME_FORMAT);
            lcd.printRow(2, "      " + time);
            lcd.printRow(3, "    Press Ctrl+C");
            Thread.sleep(1000);
        }

        lcd.setBacklight(SparkFunSerLcdDevice.Color.GREEN);
        lcd.clear();
        lcd.printRow(0, "    Demo Complete!");
        lcd.printRow(2, "   SparkFun SerLCD");
        lcd.printRow(3, "      + Robo4J");

        println("Demo complete!");
    }
}
