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

/**
 * The interface for an LCD.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */

import com.robo4j.hw.rpi.i2c.adafruitlcd.impl.AdafruitLcdImpl.Direction;
import com.robo4j.hw.rpi.utils.I2cBus;

import java.io.IOException;

public interface AdafruitLcd {
	// Not using the Pi4J bus specification here, since we may not be able to
	// load the class (we can run with a mock up).
	I2cBus DEFAULT_BUS = I2cBus.BUS_1;
	int DEFAULT_ADDRESS = 0x20;

	/**
	 * Sets the text to display. Use "\n" to begin a new line. Each line will be
	 * padded to the full 16 columns to clear anything still in the VRAM following
	 * the new line.
	 * 
	 * @param s
	 *            the text to display.
	 * @throws IOException
	 *             exception
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
	 *             exception
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
	 *             exception
	 */
	void setCursorPosition(int row, int column) throws IOException;

	/**
	 * Stop the LCD. Will turn of the backlight.
	 * 
	 * @throws IOException
	 *             exception
	 */
	void stop() throws IOException;

	/**
	 * Clear the text on the display.
	 * 
	 * @throws IOException
	 *             exception
	 */
	void clear() throws IOException;

	/**
	 * Will move the cursor home.
	 * 
	 * @throws IOException
	 *             exception
	 */
	void home() throws IOException;

	/**
	 * Will enable or disable the cursor.
	 * 
	 * @param enable
	 *            true to enable, false to disable.
	 * @throws IOException
	 *             exception
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
	 *             exception
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
	 *             exception
	 */
	void setBlinkEnabled(boolean enable) throws IOException;

	/**
	 * @return true if blink is enabled.
	 */
	boolean isBlinkEnabled();

	/**
	 * Sets the backlight color. Note that not all colors will be available on all
	 * devices.
	 * 
	 * @param color
	 *            the color to set
	 * @throws IOException
	 *             exception
	 */
	void setBacklight(Color color) throws IOException;

	/**
	 * @return the current backlight color in use.
	 * @throws IOException
	 *             exception
	 */
	Color getBacklight() throws IOException;

	/**
	 * Scrolls the display in the chosen direction.
	 * 
	 * @param direction
	 *            the direction to scroll the display.
	 * @throws IOException
	 *             exception
	 */
	void scrollDisplay(Direction direction) throws IOException;

	/**
	 * Sets the text flow direction.
	 * 
	 * @param direction
	 *            the directon for text flow.
	 * @throws IOException
	 *             exception
	 */
	void setTextFlowDirection(Direction direction) throws IOException;

	/**
	 * Enables or disables automatic scrolling.
	 * 
	 * @param enable
	 *            true to enable automatic scrolling, false to disable.
	 * @throws IOException
	 *             exception
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
	 *             exception
	 */
	boolean isButtonPressed(Button button) throws IOException;

	/**
	 * @return the bitmask for the press status of all buttons at once.
	 * 
	 * @throws IOException
	 *             exception
	 */
	int buttonsPressedBitmask() throws IOException;

	/**
	 * Full reset of the LCD.
	 * 
	 * @throws IOException
	 *             exception
	 */
	void reset() throws IOException;

	/**
	 * Fill one of the first 8 CGRAM locations with custom characters. The location
	 * parameter should be between 0 and 7 and pattern should provide an array of 8
	 * bytes containing the pattern. e.g. you can design your custom character at
	 * &lt;a
	 * href=http://www.quinapalus.com/hd44780udg.html&gt;http://www.quinapalus.com/hd44780udg.html&lt;a&gt;.
	 * To show your custom character obtain the string representation for the
	 * location e.g. String.format("custom char=%c", 0).
	 * 
	 * @param location
	 *            storage location for this character, between 0 and 7
	 * @param pattern
	 *            array of 8 bytes containing the character's pattern
	 * @throws IOException
	 *             exception
	 */
	void createChar(int location, byte[] pattern) throws IOException;
}
