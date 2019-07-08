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
 * https://learn.adafruit.com/adafruit-led-backpack/bi-color-8x8-matrix
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BiColor8x8MatrixDevice extends LEDBackpack {

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

	public void addPixels(Collection<PackElement> elements) {
		if (elements == null) {
			System.out.println("addPixels: not allowed state!");
		} else {
			for (PackElement e : elements) {
				addPixels(e);
			}
		}
	}

	public void addPixels(PackElement... elements) {
		if (elements == null || elements.length == 0) {
			System.out.println("addPixels: not allowed state!");
		} else {
			for (PackElement e : elements) {
				addPixel(e);
			}
		}
	}

	public void setRotation(MatrixRotation rotation) {
		this.rotation = rotation;
	}

	private void setPixel(PackElement element) {
		short x;
		short y;
		switch (rotation) {
		case DEFAULT_X_Y:
			x = (short) element.getX();
			y = (short) element.getY();
			break;
		case RIGHT_90:
			x = flipPosition(element.getY());
			y = flipPosition(element.getX());
			break;
		case RIGHT_180:
			x = flipPosition(element.getX());
			y = flipPosition(element.getY());
			break;
		case RIGHT_270:
			x = (short) element.getY();
			y = flipPosition(element.getX());
			break;
		case INVERSION:
			x = (short) element.getY();
			y = (short) element.getX();
			break;
		case LEFT_90:
			x = (short) element.getX();
			y = flipPosition(element.getY());
			break;
		case LEFT_180:
			x = flipPosition(element.getY());
			y = flipPosition(element.getX());
		case LEFT_270:
			x = flipPosition(element.getX());
			y = (short) element.getY();
		default:
			x = MATRIX_SIZE;
			y = MATRIX_SIZE;
		}
		setColorByMatrixToBuffer(x, y, element.getColor());
	}

	private boolean validateElement(int x, int y) {
		return x >= 0 && x < MATRIX_SIZE && y >= 0 && y < MATRIX_SIZE;
	}

	private short flipPosition(int v) {
		return (short) (MATRIX_SIZE - v - 1);
	}

}
