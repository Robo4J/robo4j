/*
 * Copyright (C) 2017. Miroslav Wengner, Marcus Hirt
 * This ButtonListener.java  is part of robo4j.
 * module: robo4j-hw-rpi
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.hw.rpi.i2c.adafruitlcd;

/**
 * A button listener interface that can be used to listen for buttons being
 * pressed on the LCD shield.
 * 
 * @see ButtonPressedObserver
 * 
 * @author Marcus Hirt
 */
public interface ButtonListener {
	/**
	 * Called when a button is pressed on the LCD shield.
	 * 
	 * @param button the button that was pressed.
	 */
	void onButtonPressed(Button button);
}
