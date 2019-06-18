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

import java.io.IOException;

/**
 * https://learn.adafruit.com/adafruit-led-backpack/bi-color-8x8-matrix
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BiColor8x8MatrixDevice extends LEDBackpack {

	private static final short MATRIX_SIZE = 8;
	private MatrixRotation rotation;

	public BiColor8x8MatrixDevice(int bus, int address, int brightness, MatrixRotation rotation) throws IOException {
		super(bus, address);
		initiate(brightness);
		this.rotation = rotation;

	}

	public BiColor8x8MatrixDevice() throws IOException {
		this(I2CBus.BUS_1, DEFAULT_I2C_ADDRESS, DEFAULT_BRIGHTNESS, MatrixRotation.ONE);
	}

	public int getMatrixSize() {
		return MATRIX_SIZE;
	}

	public void addPixel(PackElement element) {
		if (validateElement(element.getX(), element.getY())) {
			setPixel(element);
		} else {
			System.out.println(String.format("addPixel: not allowed element= %s", element));
		}
	}

	public void addPixes(PackElement... elements) {
		if (elements == null) {
			System.out.println("addPixes: not allowed state!");
		} else {
			for (PackElement e : elements) {
				addPixel(e);
			}
		}

	}

	public void setRotation(MatrixRotation rotation) {
		this.rotation = rotation;
	}

	public void clear() throws IOException {
		clearBuffer();
	}

	public void display() throws IOException {
		writeDisplay();
	}

	private void setPixel(PackElement element) {
		short x;
		short y;
		switch (rotation) {
		case ONE:
			x = (short) element.getX();
			y = (short) element.getY();
			break;
		case TWO:
			x = (short) element.getX();
			y = flipPosition(element.getY());
			break;
		case THREE:
			x = flipPosition(element.getX());
			y = (short) element.getY();
			break;
		case FOUR:
			x = flipPosition(element.getX());
			y = flipPosition(element.getY());
			break;
		case FIVE:
			x = flipPosition(element.getY());
			y = flipPosition(element.getX());
			break;
		default:
			x = MATRIX_SIZE;
			y = MATRIX_SIZE;
		}
		setColorByMatrixToBuffer(MATRIX_SIZE, x, y, element.getColor());
	}

	private boolean validateElement(int x, int y) {
		return x >= 0 && x < MATRIX_SIZE && y >= 0 && y < MATRIX_SIZE;
	}

	private short flipPosition(int v) {
		return (short) (MATRIX_SIZE - v - 1);
	}

}