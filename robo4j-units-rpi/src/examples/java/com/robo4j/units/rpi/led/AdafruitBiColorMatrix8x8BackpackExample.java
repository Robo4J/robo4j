/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.BiColor;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.PackElement;
import com.robo4j.util.SystemUtil;

import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adafruit Bi-Color 8x8 Matrix example
 * 
 * demo: Incrementally turning on a led light over the matrix diagonal. The each
 * time with different Color {@link BiColor}. The color is changing circularly.
 * 
 * https://learn.adafruit.com/adafruit-led-backpack/bi-color-8x8-matrix
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AdafruitBiColorMatrix8x8BackpackExample {
	public static void main(String[] args) throws Exception {

		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		InputStream settings = AdafruitBiColorMatrix8x8BackpackExample.class.getClassLoader()
				.getResourceAsStream("matrix8x8example.xml");
		RoboContext ctx = new RoboBuilder().add(settings).build();

		ctx.start();
		System.out.println("State after start:");
		System.out.println(SystemUtil.printStateReport(ctx));
		RoboReference<LEDBackpackMessage> barUnit = ctx.getReference("matrix");
		LEDBackpackMessage clearMessage = new LEDBackpackMessage();
		AtomicInteger position = new AtomicInteger();
		executor.scheduleAtFixedRate(() -> {
			if (position.get() > 7) {
				position.set(0);
			}
			barUnit.sendMessage(clearMessage);

			PackElement element = new PackElement(position.get(), position.getAndIncrement(),
					BiColor.getByValue(position.get() % 3 + 1));
			LEDBackpackMessage addMessage = new LEDBackpackMessage(LEDBackpackMessageType.DISPLAY);
			addMessage.addElement(element);
			barUnit.sendMessage(addMessage);

		}, 2, 1, TimeUnit.SECONDS);

		System.out.println("Press enter to quit\n");
		System.in.read();
		executor.shutdown();
		ctx.shutdown();

	}
}
