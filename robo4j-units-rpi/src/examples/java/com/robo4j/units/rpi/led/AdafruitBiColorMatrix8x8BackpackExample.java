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

package com.robo4j.units.rpi.led;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboReference;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.BiColor;
import com.robo4j.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adafruit Bi-Color 8x8 Matrix example
 * <p>
 * demo: Incrementally turning on a led light over the matrix diagonal. The each
 * time with different Color {@link BiColor}. The color is changing circularly.
 * <p>
 * https://learn.adafruit.com/adafruit-led-backpack/bi-color-8x8-matrix
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AdafruitBiColorMatrix8x8BackpackExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdafruitBiColorMatrix8x8BackpackExample.class);

    public static void main(String[] args) throws Exception {

        var executor = Executors.newSingleThreadScheduledExecutor();
        var settings = AdafruitBiColorMatrix8x8BackpackExample.class.getClassLoader().getResourceAsStream("matrix8x8example.xml");
        var ctx = new RoboBuilder().add(settings).build();

        ctx.start();
        LOGGER.info("State after start:");
        LOGGER.info(SystemUtil.printStateReport(ctx));
        RoboReference<DrawMessage> matrixUnit = ctx.getReference("matrix");
        AtomicInteger position = new AtomicInteger();
        executor.scheduleAtFixedRate(() -> {
            if (position.get() > 7) {
                position.set(0);
            }
            matrixUnit.sendMessage(DrawMessage.MESSAGE_CLEAR);

            matrixUnit.sendMessage(new DrawMessage(BackpackMessageCommand.PAINT, new short[]{(short) position.get()},
                    new short[]{(short) position.getAndIncrement()}, new BiColor[]{BiColor.getByValue(position.get() % 3 + 1)}));
        }, 2, 1, TimeUnit.SECONDS);

        LOGGER.info("Press enter to quit\n");
        System.in.read();
        executor.shutdown();
        ctx.shutdown();
    }
}
