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

package com.robo4j.hw.rpi.i2c.adafruitoled;

import com.pi4j.io.i2c.I2CBus;
import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;

import java.io.IOException;

/**
 * https://learn.adafruit.com/adafruit-led-backpack/bi-color-24-bargraph
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BiColor24BarDevice extends AbstractI2CDevice {

	private static final int DEFAULT_I2C_ADDRESS = 0x70;
	private static final int DEFAULT_BRIGHTNESS = 15;
	private static final int OSCILLATOR_TURN_ON = 0x21;
	private static final int HT16K33_BLINK_CMD = 0x80;
	private static final int HT16K33_BLINK_DISPLAY_ON = 0x01;
	private static final int HT16K33_BLINK_DISPLAY_OFF = 0;
	private static final int HT16K33_CMD_BRIGHTNESS = 0xE0;
	private static final int HT16K33_BLINK_OFF = 0x00;
	private final short[] buffer = new short[8]; // uint16_t
	private final int MAX_BARS = 24;

	public BiColor24BarDevice(int bus, int address, int brightness) throws IOException {
		super(bus, address);
		initiate(brightness);
	}

	public BiColor24BarDevice() throws IOException {
		this(I2CBus.BUS_1, DEFAULT_I2C_ADDRESS, DEFAULT_BRIGHTNESS);
	}

	public void addBar(int pos, BiColor color) {
		final BarElement element = new BarElement(pos, color);
		addBar(element);
	}

	public void addBar(BarElement element) {
		if (validatePositions(element.getX())) {
			setBar(element);
		} else {
			System.out.println(String.format("addBar: not allowed bar= %s", element));
		}
	}

	public void addBars(BarElement... elements) {
		if (elements == null || elements.length > MAX_BARS) {
			System.out.println("addBars: not allowed state!");
		} else {
			for (BarElement e : elements) {
				addBar(e);
			}
		}
	}

	public void clearBuffer() throws IOException {
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = 0;
		}
	}

	public int getMaxBar() {
		return MAX_BARS;
	}

	public void display() throws IOException {
		writeDisplay();
	}

	private void initiate(int brightness) throws IOException {
		i2cDevice.write((byte) (OSCILLATOR_TURN_ON)); // Turn on oscilator
		i2cDevice.write(blinkRate(HT16K33_BLINK_OFF));
		i2cDevice.write(setBrightness(brightness));
	}

	private short intToShort(int value) {
		return (short) (value & 0xFFFF);
	}

	private byte uint8ToByte(int value) {
		return (byte) (value & 0xFF);
	}

	// private void setBar(int bar, BiColor color) {
	private void setBar(BarElement element) {
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

		switch (element.getColor()) {
		case RED:
			// Turn on red LED.
			buffer[c] |= _BV(a);
			// Turn off green LED.
			buffer[c] &= ~_BV(intToShort(a + 8));
			break;
		case YELLOW:
			// Turn on red and green LED.
			buffer[c] |= _BV(a) | _BV(intToShort(a + 8));
			break;
		case GREEN:
			// Turn on green LED.
			buffer[c] |= _BV(intToShort(a + 8));
			// Turn off red LED.
			buffer[c] &= ~_BV(a);
			break;
		case OFF:
			// Turn off red and green LED.
			buffer[c] &= ~_BV(a) & ~_BV(intToShort(a + 8));
			break;
		default:
			System.out.println("setBar ERROR: " + element);
			break;
		}
	}

	private void writeDisplay() throws IOException {
		int address = 0;
		for (int i = 0; i < buffer.length; i++) {
			i2cDevice.write(address++, (byte) (buffer[i] & 0xFF));
			i2cDevice.write(address++, (byte) (buffer[i] >> 8));
		}
	}

	private short _BV(short i) {
		return ((short) (1 << i));
	}

	private byte setBrightness(int b) {
		if (b > 15)
			b = 15;
		return uint8ToByte(HT16K33_CMD_BRIGHTNESS | b);
	}

	private byte blinkRate(int b) {
		if (b > 3)
			b = 0;
		return uint8ToByte(HT16K33_BLINK_CMD | HT16K33_BLINK_DISPLAY_ON | (b << 1));
	}

	private boolean validatePositions(int pos) {
		return pos >= 0 && pos < MAX_BARS;
	}

}
