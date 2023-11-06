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

package com.robo4j.hw.rpi.i2c.adafruitbackpack;

import com.robo4j.hw.rpi.utils.I2cBus;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 0'54 Alphanumeric Backpack Device
 *
 * https://learn.adafruit.com/adafruit-led-backpack/0-54-alphanumeric
 * https://learn.adafruit.com/14-segment-alpha-numeric-led-featherwing?view=all
 * https://en.wikipedia.org/wiki/Fourteen-segment_display
 *
 * FONT are according to the Adafruit LED_Backpack implementation
 * https://github.com/adafruit/Adafruit_LED_Backpack
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AlphanumericDevice extends AbstractBackpack {

	public static final int POSITION_START = 0;
	public static final int POSITION_MAX = 3;
	private static int[] FONTS = { 0b0000000000000001, 0b0000000000000010, 0b0000000000000100, 0b0000000000001000, 0b0000000000010000,
			0b0000000000100000, 0b0000000001000000, 0b0000000010000000, 0b0000000100000000, 0b0000001000000000, 0b0000010000000000,
			0b0000100000000000, 0b0001000000000000, 0b0010000000000000, 0b0100000000000000, 0b1000000000000000, 0b0000000000000000,
			0b0000000000000000, 0b0000000000000000, 0b0000000000000000, 0b0000000000000000, 0b0000000000000000, 0b0000000000000000,
			0b0000000000000000, 0b0001001011001001, 0b0001010111000000, 0b0001001011111001, 0b0000000011100011, 0b0000010100110000,
			0b0001001011001000, 0b0011101000000000, 0b0001011100000000, 0b0000000000000000, //
			0b0000000000000110, // !
			0b0000001000100000, // "
			0b0001001011001110, // #
			0b0001001011101101, // $
			0b0000110000100100, // %
			0b0010001101011101, // &
			0b0000010000000000, // '
			0b0010010000000000, // (
			0b0000100100000000, // )
			0b0011111111000000, // *
			0b0001001011000000, // +
			0b0000100000000000, // ,
			0b0000000011000000, // -
			0b0000000000000000, // .
			0b0000110000000000, // /
			0b0000110000111111, // 0
			0b0000000000000110, // 1
			0b0000000011011011, // 2
			0b0000000010001111, // 3
			0b0000000011100110, // 4
			0b0010000001101001, // 5
			0b0000000011111101, // 6
			0b0000000000000111, // 7
			0b0000000011111111, // 8
			0b0000000011101111, // 9
			0b0001001000000000, // :
			0b0000101000000000, // ;
			0b0010010000000000, // <
			0b0000000011001000, // =
			0b0000100100000000, // >
			0b0001000010000011, // ?
			0b0000001010111011, // @
			0b0000000011110111, // A
			0b0001001010001111, // B
			0b0000000000111001, // C
			0b0001001000001111, // D
			0b0000000011111001, // E
			0b0000000001110001, // F
			0b0000000010111101, // G
			0b0000000011110110, // H
			0b0001001000000000, // I
			0b0000000000011110, // J
			0b0010010001110000, // K
			0b0000000000111000, // L
			0b0000010100110110, // M
			0b0010000100110110, // N
			0b0000000000111111, // O
			0b0000000011110011, // P
			0b0010000000111111, // Q
			0b0010000011110011, // R
			0b0000000011101101, // S
			0b0001001000000001, // T
			0b0000000000111110, // U
			0b0000110000110000, // V
			0b0010100000110110, // W
			0b0010110100000000, // X
			0b0001010100000000, // Y
			0b0000110000001001, // Z
			0b0000000000111001, // [
			0b0010000100000000, //
			0b0000000000001111, // ]
			0b0000110000000011, // ^
			0b0000000000001000, // _
			0b0000000100000000, // `
			0b0001000001011000, // a
			0b0010000001111000, // b
			0b0000000011011000, // c
			0b0000100010001110, // d
			0b0000100001011000, // e
			0b0000000001110001, // f
			0b0000010010001110, // g
			0b0001000001110000, // h
			0b0001000000000000, // i
			0b0000000000001110, // j
			0b0011011000000000, // k
			0b0000000000110000, // l
			0b0001000011010100, // m
			0b0001000001010000, // n
			0b0000000011011100, // o
			0b0000000101110000, // p
			0b0000010010000110, // q
			0b0000000001010000, // r
			0b0010000010001000, // s
			0b0000000001111000, // t
			0b0000000000011100, // u
			0b0010000000000100, // v
			0b0010100000010100, // w
			0b0010100011000000, // x
			0b0010000000001100, // y
			0b0000100001001000, // z
			0b0000100101001001, // {
			0b0001001000000000, // |
			0b0010010010001001, // }
			0b0000010100100000, // ~
			0b0011111111111111,

	};

	private final AtomicInteger position = new AtomicInteger(0);

	public AlphanumericDevice() throws IOException {
		this(I2cBus.BUS_1, 0x70, DEFAULT_BRIGHTNESS);
	}

	public AlphanumericDevice(I2cBus bus, int address, int brightness) throws IOException {
		super(bus, address, brightness);
	}

	public void addCharacter(char c, boolean dp) {
		setCharacter(incrementPosition(), FONTS[c], dp);
	}

	public void setCharacter(int pos, char c, boolean dp) {
		if (!validPosition(pos)) {
			throw new IllegalArgumentException("Position out of bounds. pos=" + pos);
		}
		setCharacter(pos, FONTS[c], dp);
		position.set(pos);
	}

	public void addValue(short v, boolean dp) {
		setValue(incrementPosition(), v, dp);
	}

	public void addValue(int pos, short v, boolean dp) {
		if (validPosition(pos)) {
			setValue(pos, v, dp);
			position.set(pos);
		}
	}

	/**
	 * @return the number of characters on the display.
	 */
	public int getNumberOfCharacters() {
		return POSITION_MAX + 1;
	}

	private boolean validPosition(int p) {
		return p >= POSITION_START && p <= POSITION_MAX;
	}

	private int incrementPosition() {
		if (position.get() > POSITION_MAX) {
			position.set(POSITION_START);
		}
		return position.getAndIncrement();
	}

}
