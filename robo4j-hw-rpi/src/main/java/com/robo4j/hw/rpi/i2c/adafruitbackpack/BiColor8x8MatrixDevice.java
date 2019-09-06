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

/**
 * Implementation of Adafruit BiColor 8x8 Matrix
 * https://learn.adafruit.com/adafruit-led-backpack/bi-color-8x8-matrix
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BiColor8x8MatrixDevice extends AbstractBackpack implements MatrixLedDevice {

	private static final short MATRIX_SIZE = 8;
	private MatrixRotation rotation;

	public BiColor8x8MatrixDevice(int bus, int address, int brightness, MatrixRotation rotation) throws IOException {
		super(bus, address, brightness);
		this.rotation = rotation;

	}

	public BiColor8x8MatrixDevice(int bus, int address, int brightness) throws IOException {
		super(bus, address, brightness);
		this.rotation = MatrixRotation.DEFAULT_X_Y;

	}

	public BiColor8x8MatrixDevice() throws IOException {
		super();
		this.rotation = MatrixRotation.DEFAULT_X_Y;
	}

	/**
	 * Draws the selected pixel in the selected color
	 * 
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 * @param color
	 *            the color to draw
	 */
	public void drawPixel(short x, short y, BiColor color) {
		if (!validate(x, y)) {
			throw new IllegalArgumentException("x and/or y out of bounds. x=" + x + " y=" + y);
		}

		switch (rotation) {
		case DEFAULT_X_Y:
			break;
		case RIGHT_90:
			x = flipPosition(y);
			y = flipPosition(x);
			break;
		case RIGHT_180:
			x = flipPosition(x);
			y = flipPosition(y);
			break;
		case RIGHT_270:
			x = y;
			y = flipPosition(x);
			break;
		case INVERSION:
			x = y;
			y = x;
			break;
		case LEFT_90:
			y = flipPosition(y);
			break;
		case LEFT_180:
			x = flipPosition(y);
			y = flipPosition(x);
			break;
		case LEFT_270:
			x = flipPosition(x);
			break;
		default:
			x = MATRIX_SIZE;
			y = MATRIX_SIZE;
		}
		setColorByMatrixToBuffer(x, y, color);
	}

	public void drawPixel(int x, int y, BiColor color) {
		drawPixel((short) x, (short) y, color);
	}

	public void drawPixels(short[] xs, short[] ys, BiColor[] colors) {
		if (xs.length != ys.length || ys.length != colors.length) {
			throw new IllegalArgumentException("All arrays must be same length. xs.length=" + xs.length + " ys.length=" + ys.length
					+ " colors.length" + colors.length);
		}

		for (int i = 0; i < xs.length; i++) {
			drawPixel(xs[i], ys[i], colors[i]);
		}
	}

	public void setRotation(MatrixRotation rotation) {
		this.rotation = rotation;
	}

	private boolean validate(int x, int y) {
		return x >= 0 && x < MATRIX_SIZE && y >= 0 && y < MATRIX_SIZE;
	}

	private short flipPosition(int v) {
		return (short) (MATRIX_SIZE - v - 1);
	}

	@Override
	public int getHeight() {
		return MATRIX_SIZE;
	}

	@Override
	public int getWidth() {
		return MATRIX_SIZE;
	}

}
