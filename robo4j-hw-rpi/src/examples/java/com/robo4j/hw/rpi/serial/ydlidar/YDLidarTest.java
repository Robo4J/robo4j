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
package com.robo4j.hw.rpi.serial.ydlidar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Example. Gets some information from the lidar, prints it, and then captures
 * data for 10 seconds.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class YDLidarTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(YDLidarTest.class);

    //TODO: review example with sleep
    public static void main(String[] args) throws IOException, InterruptedException, TimeoutException {
        YDLidarDevice device = new YDLidarDevice(scanResult -> LOGGER.info("Got scan result:{}", scanResult));
        LOGGER.info("Restarting the device to make sure we start with a clean slate");
        device.restart();
        // Takes some serious time to restart this thing ;)
        LOGGER.info("Waiting 4s for the device to properly boot up");
        Thread.sleep(4000);
        LOGGER.info("device:{}", device);
        LOGGER.info("deviceInfo:{}", device.getDeviceInfo());
        LOGGER.info("deviceHealth:{}", device.getHealthInfo());
        LOGGER.info("Ranging Frequency:{}", device.getRangingFrequency());
        LOGGER.info("Will capture data for 10 seconds...");
        device.setScanning(true);
        Thread.sleep(10000);
        device.setScanning(false);
        device.shutdown();
        LOGGER.info("Done!");
    }

}
