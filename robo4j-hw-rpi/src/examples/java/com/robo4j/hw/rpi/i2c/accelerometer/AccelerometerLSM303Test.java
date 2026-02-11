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
package com.robo4j.hw.rpi.i2c.accelerometer;

import com.robo4j.hw.rpi.i2c.ReadableDevice;
import com.robo4j.math.geometry.Tuple3f;

import java.io.IOException;

import static java.lang.IO.*;

/**
 * Example useful to check if your accelerometer is working properly.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AccelerometerLSM303Test {

    public static void main(String[] args) throws IOException, InterruptedException {
        ReadableDevice<Tuple3f> device = new AccelerometerLSM303Device();
        getReading(device, "Place the device in the position(s) you want to measure");
    }

    private static void getReading(ReadableDevice<Tuple3f> device, String message)
            throws IOException, InterruptedException {
        prompt(message);
        printStats(readValues(device));
    }

    private static void printStats(Stats stats) {
        println("Result:" + stats);
    }

    private static void prompt(String msg) throws IOException {
        println(msg);
        println("Press <Enter> to continue!");
        readln();
    }

    private static Stats readValues(ReadableDevice<Tuple3f> device) throws IOException, InterruptedException {
        // TODO: change print...
        Stats stats = new Stats();
        for (int i = 0; i < 250; i++) {
            Tuple3f fl = device.read();
            stats.addValue(fl);
            Thread.sleep(20);
            if (i % 25 == 0) {
                print(".");
            }
        }
        println();
        return stats;
    }
}
