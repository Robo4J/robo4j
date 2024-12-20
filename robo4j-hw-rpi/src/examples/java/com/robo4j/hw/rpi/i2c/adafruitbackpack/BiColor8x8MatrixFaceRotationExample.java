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

package com.robo4j.hw.rpi.i2c.adafruitbackpack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Simple example using {@link BiColor8x8MatrixDevice}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BiColor8x8MatrixFaceRotationExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(BiColor8x8MatrixFaceRotationExample.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        LOGGER.debug("=== BiColor 8x8 Matrix Face Rotation Example ===");

        BiColor8x8MatrixDevice ledMatrix = new BiColor8x8MatrixDevice();
        //@formatter:off
        byte[] faceSmile = {
        		0b0011_1100,
        		0b0100_0010,
        		(byte)0b1010_0101,
        		(byte)0b1000_0001,
        		(byte)0b1010_0101,
        		(byte)0b1001_1001,
	        	(byte)0b0100_0010,
	            (byte)0b0011_1100
        };
        //@formatter:on

        for (int i = 0; i < faceSmile.length; i++) {
            ledMatrix.clear();
            ledMatrix.display();
            LedBackpackUtils.paintToByRowArraysAndColor(ledMatrix, faceSmile, BiColor.getByValue(i % 2 + 1));
            ledMatrix.display();
            ledMatrix.setRotation(MatrixRotation.getById(i % 5 + 1));
            TimeUnit.SECONDS.sleep(1);
        }

        LOGGER.debug("Press <Enter> to quit!");
        System.in.read();
        ledMatrix.clear();
        ledMatrix.display();
    }
}
