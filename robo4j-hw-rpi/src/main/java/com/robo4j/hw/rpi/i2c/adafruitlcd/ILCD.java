/*
 * Copyright (C) 2014-2017. Miroslav Wengner, Marcus Hirt
 * This ILCD.java  is part of robo4j.
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
 * The interface for an LCD.
 */
import java.io.IOException;

import com.robo4j.hw.rpi.i2c.adafruitlcd.impl.RealLCD.Direction;

public interface ILCD {

	/**
	 * Sets the text to display.
	 * 
	 * @param s
	 *            the text to display.
	 * @throws IOException
	 */
	void setText(String s) throws IOException;

	/**
	 * Sets the text to display on the defined row.
	 * 
	 * @param row
	 *            the row at which to display the text.
	 * @param string
	 *            the string to display.
	 * @throws IOException
	 */
	void setText(int row, String string) throws IOException;

	/**
	 * Move the cursor to the defined row and column.
	 * 
	 * @param row
	 *            the row to move the cursor to.
	 * @param column
	 *            the column to move the cursor to.
	 * @throws IOException
	 */
	void setCursorPosition(int row, int column) throws IOException;

	/**
	 * Stop the LCD. Will turn of the backlight.
	 * 
	 * @throws IOException
	 */
	void stop() throws IOException;

	/**
	 * Clear the text on the display.
	 * 
	 * @throws IOException
	 */
	void clear() throws IOException;

	/**
	 * Will move the cursor home.
	 * 
	 * @throws IOException
	 */
	void home() throws IOException;

	/**
	 * Will enable or disable the cursor.
	 * 
	 * @param enable
	 *            true to enable, false to disable.
	 * @throws IOException
	 */
	void setCursorEnabled(boolean enable) throws IOException;

	/**
	 * @return true if the cursor is enabled, false if it is disabled.
	 */
	boolean isCursorEnabled();

	/**
	 * Will enable or disable the display.
	 * 
	 * @param enable
	 *            true to enable, false to disable.
	 * @throws IOException
	 */
	void setDisplayEnabled(boolean enable) throws IOException;

	/**
	 * @return true if the display is enabled, false otherwise.
	 */
	boolean isDisplayEnabled();

	/**
	 * @param enable
	 *            to enable blink, false to disable.
	 * @throws IOException
	 */
	void setBlinkEnabled(boolean enable) throws IOException;

	/**
	 * @return true if blink is enabled.
	 */
	boolean isBlinkEnabled();

	/**
	 * Sets the backlight color. Note that not all colors will be available on
	 * all devices.
	 * 
	 * @param color
	 *            the color to set
	 * @throws IOException
	 */
	void setBacklight(Color color) throws IOException;

	/**
	 * @return the current backlight color in use.
	 * @throws IOException
	 */
	Color getBacklight() throws IOException;

	/**
	 * Scrolls the display in the chosen direction.
	 * 
	 * @param direction
	 *            the direction to scroll the display.
	 * @throws IOException
	 */
	void scrollDisplay(Direction direction) throws IOException;

	/**
	 * Sets the text flow direction.
	 * 
	 * @param direction
	 *            the directon for text flow.
	 * @throws IOException
	 */
	void setTextFlowDirection(Direction direction) throws IOException;

	/**
	 * Enables or disables automatic scrolling.
	 * 
	 * @param enable
	 *            true to enable automatic scrolling, false to disable.
	 * @throws IOException
	 */
	void setAutoScrollEnabled(boolean enable) throws IOException;

	/**
	 * @return true if automatic scrolling is enabled, false otherwise.
	 */
	boolean isAutoScrollEnabled();

	/**
	 * @param button
	 *            the button to check.
	 * @return true if the button is currently depressed, false otherwise.
	 * @throws IOException
	 */
	boolean isButtonPressed(Button button) throws IOException;

	/**
	 * @return the bitmask for the press status of all buttons at once.
	 * 
	 * @throws IOException
	 */
	int buttonsPressedBitmask() throws IOException;

	/**
	 * Full reset of the LCD.
	 * 
	 * @throws IOException
	 */
	void reset() throws IOException;
}