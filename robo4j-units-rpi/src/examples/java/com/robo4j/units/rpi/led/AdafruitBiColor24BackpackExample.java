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

import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.BiColor;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.BiColor24BarDevice;

/**
 * Adafruit Bi-Color 24 Bargraph example
 *
 * demo: Incrementally turning on a led light over 24 available diodes. The each
 * time with different Color {@link BiColor}. The color is changing circularly.
 *
 * https://learn.adafruit.com/adafruit-led-backpack/bi-color-24-bargraph
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AdafruitBiColor24BackpackExample {

	public static void main(String[] args) throws Exception {
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		InputStream settings = AdafruitBiColor24BackpackExample.class.getClassLoader().getResourceAsStream("bargraph24example.xml");
		RoboContext ctx = new RoboBuilder().add(settings).build();

		ctx.start();
		RoboReference<DrawMessage> barUnit = ctx.getReference("bargraph");
		DrawMessage clearMessage = new DrawMessage(BackpackMessageCommand.CLEAR);
		AtomicInteger position = new AtomicInteger();
		executor.scheduleAtFixedRate(() -> {
			if (position.get() > BiColor24BarDevice.MAX_BARS - 1) {
				position.set(0);
			}
			barUnit.sendMessage(clearMessage);
			barUnit.sendMessage(new DrawMessage(BackpackMessageCommand.PAINT, new short[] { (short) position.getAndIncrement() },
					new short[] { 0 }, new BiColor[] { BiColor.getByValue(position.get() % 3 + 1) }));
		}, 2, 1, TimeUnit.SECONDS);

		System.out.println("Press enter to quit\n");
		System.in.read();
		executor.shutdown();
		ctx.shutdown();

	}
}
