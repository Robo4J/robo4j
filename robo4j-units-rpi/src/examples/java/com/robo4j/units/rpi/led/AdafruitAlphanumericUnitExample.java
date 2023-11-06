/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;

/**
 * https://learn.adafruit.com/adafruit-led-backpack/0-54-alphanumeric
 *
 * demo: Continually sending defined String
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AdafruitAlphanumericUnitExample {

	private static final byte[] MESSAGE = "Hello Robo4j World! ".getBytes(Charset.forName("ISO646-US"));
	private static byte[] BUFFER = { ' ', ' ', ' ', ' ' };

	public static void main(String[] args) throws Exception {
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		InputStream settings = AdafruitBiColor24BackpackExample.class.getClassLoader().getResourceAsStream("alphanumericexample.xml");
		RoboContext ctx = new RoboBuilder().add(settings).build();

		ctx.start();
		RoboReference<AlphaNumericMessage> alphaUnit = ctx.getReference("alphanumeric");
		AtomicInteger textPosition = new AtomicInteger();

		executor.scheduleAtFixedRate(() -> {

			if (textPosition.getAndIncrement() >= MESSAGE.length - 1) {
				textPosition.set(0);
			}

			alphaUnit.sendMessage(AlphaNumericMessage.MESSAGE_CLEAR);

			byte currentChar = MESSAGE[textPosition.get()];
			adjustBuffer(currentChar);
			alphaUnit.sendMessage(new AlphaNumericMessage(BackpackMessageCommand.PAINT, BUFFER.clone(), new boolean[4], 0));
			alphaUnit.sendMessage(AlphaNumericMessage.MESSAGE_DISPLAY);
		}, 1, 500, TimeUnit.MILLISECONDS);

		System.out.println("Press enter to quit\n");
		System.in.read();
		alphaUnit.sendMessage(AlphaNumericMessage.MESSAGE_CLEAR);
		alphaUnit.sendMessage(AlphaNumericMessage.MESSAGE_DISPLAY);
		executor.shutdown();
		ctx.shutdown();

	}

	private static void adjustBuffer(byte currentChar) {

		BUFFER[3] = BUFFER[2];
		BUFFER[2] = BUFFER[1];
		BUFFER[1] = BUFFER[0];
		BUFFER[0] = currentChar;

	}
}
