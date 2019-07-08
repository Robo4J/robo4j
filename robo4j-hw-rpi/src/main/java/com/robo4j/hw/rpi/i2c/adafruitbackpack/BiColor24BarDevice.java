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

package com.robo4j.hw.rpi.i2c.adafruitbackpack;

import java.io.IOException;
import java.util.Collection;

/**
 * https://learn.adafruit.com/adafruit-led-backpack/bi-color-24-bargraph
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BiColor24BarDevice extends LEDBackpack {

	public static final int MAX_BARS = 24;

	public BiColor24BarDevice(int bus, int address, int brightness) throws IOException {
		super(bus, address, brightness);
	}

	public BiColor24BarDevice() throws IOException {
		super();
	}

	public void addBar(int pos, BiColor color) {
		final PackElement element = new PackElement(pos, color);
		addBar(element);
	}

	public void addBar(PackElement element) {
		if (validatePositions(element.getX())) {
			setBar(element);
		} else {
			System.out.println(String.format("addBar: not allowed bar= %s", element));
		}
	}

	public void addBars(Collection<PackElement> elements) {
		if (elements == null || elements.size() > MAX_BARS) {
			System.out.println("addBars: not allowed state!");
		} else {
			for (PackElement e : elements) {
				addBar(e);
			}
		}
	}

	public void addBars(PackElement... elements) {
		if (elements == null || elements.length > MAX_BARS) {
			System.out.println("addBars: not allowed state!");
		} else {
			for (PackElement e : elements) {
				addBar(e);
			}
		}
	}

	public int getMaxBar() {
		return MAX_BARS;
	}


	// private void setBar(int bar, BiColor color) {
	private void setBar(PackElement element) {
		final int bar = element.getX();
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

		setColorToBarBuffer(a, c, element.getColor());
	}

	private boolean validatePositions(int pos) {
		return pos >= 0 && pos < MAX_BARS;
	}

}
