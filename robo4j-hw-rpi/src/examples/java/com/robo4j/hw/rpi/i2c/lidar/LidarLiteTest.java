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
package com.robo4j.hw.rpi.i2c.lidar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This example will repeatedly acquire the range with the LidarLite. Good for
 * testing that it works.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class LidarLiteTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LidarLiteTest.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        LidarLiteDevice ld = new LidarLiteDevice();
        while (true) {
            ld.acquireRange();
            Thread.sleep(100);
            LOGGER.info("Distance: {}m", ld.readDistance());
            Thread.sleep(500);
        }
    }

}
