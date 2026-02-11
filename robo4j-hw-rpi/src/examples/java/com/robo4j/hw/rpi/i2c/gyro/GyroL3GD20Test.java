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
package com.robo4j.hw.rpi.i2c.gyro;

import com.robo4j.hw.rpi.i2c.gyro.GyroL3GD20Device.Sensitivity;

import java.io.IOException;

import static java.lang.IO.*;

/**
 * Simple example which repeatedly reads the gyro. Good for checking that your
 * gyro is working.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class GyroL3GD20Test {

    public static void main(String[] args) throws IOException, InterruptedException {
        println("Initializing...");
        GyroL3GD20Device device = new GyroL3GD20Device(Sensitivity.DPS_245);

        while (true) {
            println("device,read:" + device.read());
            Thread.sleep(200);
        }
    }
}
