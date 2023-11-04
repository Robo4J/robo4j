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
package com.robo4j.hw.rpi.i2c.adafruitlcd;

import java.util.HashSet;
import java.util.Set;

import com.robo4j.hw.rpi.i2c.adafruitlcd.impl.AdafruitLcdImpl;

/**
 * Enumeration of the Buttons on the LCD shield.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum Button {

	// @formatter:off
	SELECT		(0),
	RIGHT		(1),
	DOWN		(2),
	UP			(3),
	LEFT		(4);
	// @formatter:on

	// Port expander input pin definition
	private final int pin;

	Button(int pin) {
		this.pin = pin;
	}

	/**
	 * The pin corresponding to the button.
	 * 
	 * @return the pin of the button.
	 */
	public int getPin() {
		return pin;
	}

	/**
	 * Checks if a button is pressed, given an input mask.
	 * 
	 * @param mask
	 *            the input mask.
	 * @return true if the button is pressed, false otherwise.
	 * 
	 * @see AdafruitLcdImpl#buttonsPressedBitmask()
	 */
	public boolean isButtonPressed(int mask) {
		return ((mask >> getPin()) & 1) > 0;
	}

	/**
	 * Returns a set of the buttons that are pressed, according to the input
	 * mask.
	 * 
	 * @param mask
	 *            the input mask.
	 * @return a set of the buttons pressed.
	 * 
	 * @see AdafruitLcdImpl#buttonsPressedBitmask()
	 */
	public static Set<Button> getButtonsPressed(int mask) {
		Set<Button> buttons = new HashSet<>();
		for (Button button : values()) {
			if (button.isButtonPressed(mask)) {
				buttons.add(button);
			}
		}
		return buttons;
	}
}