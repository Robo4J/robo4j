/*
 * Copyright (c) 2014, 2026, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.hw.rpi.serial.ydlidar;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static java.lang.IO.*;

/**
 * Example. Gets some information from the lidar, prints it, and then captures
 * data for 10 seconds.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class YDLidarTest {

    //TODO: review example with sleep
    public static void main(String[] args) throws IOException, InterruptedException, TimeoutException {
        YDLidarDevice device = new YDLidarDevice(scanResult -> println("Got scan result:" + scanResult));
        println("Restarting the device to make sure we start with a clean slate");
        device.restart();
        // Takes some serious time to restart this thing ;)
        println("Waiting 4s for the device to properly boot up");
        Thread.sleep(4000);
        println("device:" + device);
        println("deviceInfo:" + device.getDeviceInfo());
        println("deviceHealth:" + device.getHealthInfo());
        println("Ranging Frequency:" + device.getRangingFrequency());
        println("Will capture data for 10 seconds...");
        device.setScanning(true);
        Thread.sleep(10000);
        device.setScanning(false);
        device.shutdown();
        println("Done!");
    }

}
