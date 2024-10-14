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

import com.robo4j.hw.rpi.utils.I2cBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The implementation of Adafruit BiColor bargraph
 *
 * https://learn.adafruit.com/adafruit-led-backpack/bi-color-24-bargraph
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BiColor24BarDevice extends AbstractBackpack {
	private static final Logger LOGGER = LoggerFactory.getLogger(BiColor24BarDevice.class);
	public static final int MAX_BARS = 24;

	public BiColor24BarDevice(I2cBus bus, int address, int brightness) throws IOException {
		super(bus, address, brightness);
	}

	public BiColor24BarDevice() throws IOException {
		super();
	}

	public void setBar(int bar, BiColor color) {
		if (!validatePosition(bar)) {
            LOGGER.debug("addBar: not allowed bar= %s{}", bar);
			return;
		}
		short a, c;

		if (bar < 12) {
			c = intToShort(bar / 4);
		} else {
			c = intToShort((bar - 12) / 4);
		}

		a = intToShort(bar % 4);
		if (bar >= 12) {
			a = intToShort(a + 4);
		}

		setColorToBarBuffer(a, c, color);
	}

	public void setBars(int[] bars, BiColor[] colors) {
		if (bars.length != colors.length) {
			throw new IllegalArgumentException(
					"Length of the arrays must be equal! bars.length=" + bars.length + " colors.length=" + colors.length);
		}

		for (int i = 0; i < bars.length; i++) {
			setBar(bars[i], colors[i]);
		}
	}

	public int getMaxBar() {
		return MAX_BARS;
	}

	private boolean validatePosition(int pos) {
		return pos >= 0 && pos < MAX_BARS;
	}
}
