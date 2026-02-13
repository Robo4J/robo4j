/*
 * Copyright (c) 2026, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.hw.rpi.i2c.sparkfun;

import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;
import com.robo4j.hw.rpi.utils.I2cBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Driver for SparkFun SerLCD 20x4 RGB Backlight display (Qwiic).
 * <p>
 * The SerLCD is a simple, serial-enabled LCD that provides a simple and
 * cost-effective solution for adding a 20x4 RGB character display over I2C.
 * <p>
 * Features:
 * <ul>
 *   <li>20 columns x 4 rows character display</li>
 *   <li>RGB backlight with 30 brightness levels per channel</li>
 *   <li>I2C interface via Qwiic connector</li>
 *   <li>Adjustable contrast</li>
 *   <li>Custom character support (8 characters)</li>
 * </ul>
 * <p>
 * This class is not thread-safe. When used from multiple threads, external
 * synchronization is required. When used with Robo4J, consider using
 * {@code @CriticalSectionTrait} on the wrapping RoboUnit.
 *
 * @see <a href="https://www.sparkfun.com/products/16398">SparkFun 20x4 SerLCD</a>
 * @see <a href="https://github.com/sparkfun/SparkFun_SerLCD_Arduino_Library">Arduino Library</a>
 * @author Marcus Hirt (@hirt)
 */
public final class SparkFunSerLcdDevice extends AbstractI2CDevice {
    private static final Logger LOGGER = LoggerFactory.getLogger(SparkFunSerLcdDevice.class);

    public static final int DEFAULT_I2C_ADDRESS = 0x72;
    public static final int COLUMNS = 20;
    public static final int ROWS = 4;

    // Command prefixes
    private static final byte SPECIAL_COMMAND = (byte) 0xFE;  // HD44780 commands
    private static final byte SETTING_COMMAND = (byte) 0x7C;  // SerLCD settings (pipe char)

    private static final byte LCD_RETURN_HOME = 0x02;
    private static final byte LCD_ENTRY_MODE_SET = 0x04;
    private static final byte LCD_DISPLAY_CONTROL = 0x08;
    private static final byte LCD_CURSOR_SHIFT = 0x10;
    private static final byte LCD_SET_CGRAM_ADDR = 0x40;
    private static final byte LCD_SET_DDRAM_ADDR = (byte) 0x80;

    private static final byte ENTRY_LEFT = 0x02;
    private static final byte ENTRY_SHIFT_INCREMENT = 0x01;

    private static final byte DISPLAY_ON = 0x04;
    private static final byte CURSOR_ON = 0x02;
    private static final byte BLINK_ON = 0x01;

    private static final byte DISPLAY_MOVE = 0x08;
    private static final byte MOVE_RIGHT = 0x04;

    private static final byte CLEAR_COMMAND = 0x2D;
    private static final byte CONTRAST_COMMAND = 0x18;
    private static final byte ADDRESS_COMMAND = 0x19;
    private static final byte SET_RGB_COMMAND = 0x2B;
    private static final byte ENABLE_SYSTEM_MSG = 0x2E;
    private static final byte DISABLE_SYSTEM_MSG = 0x2F;
    private static final byte ENABLE_SPLASH = 0x30;
    private static final byte DISABLE_SPLASH = 0x31;
    private static final byte SAVE_SPLASH = 0x0A;
    private static final byte CREATE_CHAR_BASE = 27;

    // Row offsets for 20x4 display (HD44780 DDRAM addresses)
    private static final int[] ROW_OFFSETS = {0x00, 0x40, 0x14, 0x54};

    // RGB backlight base offsets (each color has 30 levels: 0-29)
    private static final int RED_BASE = 128;
    private static final int GREEN_BASE = 158;
    private static final int BLUE_BASE = 188;
    private static final int COLOR_LEVELS = 29;

    private static final int COMMAND_DELAY_MS = 10;
    private static final int SPECIAL_DELAY_MS = 100;

    private byte displayControl;
    private byte entryMode;

    /**
     * Constructs a SparkFunSerLcdDevice using default settings (I2C BUS_1, address 0x72).
     *
     * @throws IOException if there was a communication problem
     */
    public SparkFunSerLcdDevice() throws IOException {
        this(I2cBus.BUS_1, DEFAULT_I2C_ADDRESS);
    }

    /**
     * Constructs a SparkFunSerLcdDevice with the specified I2C bus and address.
     *
     * @param bus     the I2C bus to use
     * @param address the I2C address of the device
     * @throws IOException if there was a communication problem
     */
    public SparkFunSerLcdDevice(I2cBus bus, int address) throws IOException {
        super(bus, address);
        initialize();
    }

    /**
     * Initializes the display with default settings.
     */
    private void initialize() throws IOException {
        sleep(SPECIAL_DELAY_MS);

        displayControl = DISPLAY_ON;
        entryMode = ENTRY_LEFT;

        specialCommand((byte) (LCD_ENTRY_MODE_SET | entryMode));
        specialCommand((byte) (LCD_DISPLAY_CONTROL | displayControl));
        clear();

        LOGGER.info("SparkFun SerLCD initialized at address 0x{}", Integer.toHexString(getAddress()));
    }

    /**
     * Clears the display and moves the cursor to home position.
     *
     * @throws IOException if there was a communication problem
     */
    public void clear() throws IOException {
        settingCommand(CLEAR_COMMAND);
        sleep(COMMAND_DELAY_MS);
    }

    /**
     * Moves the cursor to the home position (0, 0).
     * <p>
     * Note: The HD44780 RETURN_HOME command takes up to 1.52ms to execute.
     *
     * @throws IOException if there was a communication problem
     */
    public void home() throws IOException {
        specialCommand(LCD_RETURN_HOME);
    }

    /**
     * Sets the cursor position.
     *
     * @param column the column (0 to COLUMNS-1)
     * @param row    the row (0 to ROWS-1)
     * @throws IOException if there was a communication problem
     */
    public void setCursor(int column, int row) throws IOException {
        row = Math.max(0, Math.min(row, ROWS - 1));
        column = Math.max(0, Math.min(column, COLUMNS - 1));

        int position = column + ROW_OFFSETS[row];
        specialCommand((byte) (LCD_SET_DDRAM_ADDR | position));
    }

    /**
     * Writes a string to the display at the current cursor position.
     *
     * @param text the text to display
     * @throws IOException if there was a communication problem
     */
    public void print(String text) throws IOException {
        if (text == null || text.isEmpty()) {
            return;
        }
        writeBytes(text.getBytes(StandardCharsets.ISO_8859_1));
        sleep(COMMAND_DELAY_MS);
    }

    /**
     * Writes a single character to the display at the current cursor position.
     *
     * @param c the character to display
     * @throws IOException if there was a communication problem
     */
    public void print(char c) throws IOException {
        writeByte((byte) c);
        sleep(COMMAND_DELAY_MS);
    }

    /**
     * Writes a custom character (previously created with {@link #createChar}) to the display.
     *
     * @param location the custom character location (0-7)
     * @throws IOException if there was a communication problem
     */
    public void writeChar(int location) throws IOException {
        if (location < 0 || location > 7) {
            throw new IllegalArgumentException("Location must be 0-7");
        }
        writeByte((byte) location);
        sleep(COMMAND_DELAY_MS);
    }

    /**
     * Writes a string to a specific row, clearing the row first.
     *
     * @param row  the row (0 to ROWS-1)
     * @param text the text to display
     * @throws IOException if there was a communication problem
     */
    public void printRow(int row, String text) throws IOException {
        setCursor(0, row);
        String padded = String.format("%-" + COLUMNS + "s", text);
        if (padded.length() > COLUMNS) {
            padded = padded.substring(0, COLUMNS);
        }
        print(padded);
    }

    /**
     * Sets the RGB backlight color.
     * <p>
     * Each color channel has 30 brightness levels (0-29), mapped from the 0-255 input range.
     *
     * @param red   red component (0-255)
     * @param green green component (0-255)
     * @param blue  blue component (0-255)
     * @throws IOException if there was a communication problem
     */
    public void setBacklight(int red, int green, int blue) throws IOException {
        red = Math.max(0, Math.min(255, red));
        green = Math.max(0, Math.min(255, green));
        blue = Math.max(0, Math.min(255, blue));

        byte r = (byte) (RED_BASE + (red * COLOR_LEVELS / 255));
        byte g = (byte) (GREEN_BASE + (green * COLOR_LEVELS / 255));
        byte b = (byte) (BLUE_BASE + (blue * COLOR_LEVELS / 255));

        writeBytes(new byte[]{SETTING_COMMAND, SET_RGB_COMMAND, r, g, b});
        sleep(COMMAND_DELAY_MS);
    }

    /**
     * Sets the RGB backlight using direct register values.
     * <p>
     * This method sends the register values directly without the 0-255 to 30-level
     * mapping. Use values in ranges: red 128-157, green 158-187, blue 188-217.
     * This is useful for fine-tuned control or animations.
     *
     * @param red   red register value (128-157)
     * @param green green register value (158-187)
     * @param blue  blue register value (188-217)
     * @throws IOException if there was a communication problem
     */
    public void setFastBacklight(int red, int green, int blue) throws IOException {
        writeBytes(new byte[]{
                SETTING_COMMAND,
                SET_RGB_COMMAND,
                (byte) red,
                (byte) green,
                (byte) blue
        });
        sleep(COMMAND_DELAY_MS);
    }

    /**
     * Sets the backlight to a predefined color.
     *
     * @param color the color to set
     * @throws IOException if there was a communication problem
     */
    public void setBacklight(Color color) throws IOException {
        setBacklight(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Turns the backlight off.
     *
     * @throws IOException if there was a communication problem
     */
    public void setBacklightOff() throws IOException {
        setBacklight(0, 0, 0);
    }

    /**
     * Sets the backlight to white at full brightness.
     *
     * @throws IOException if there was a communication problem
     */
    public void setBacklightWhite() throws IOException {
        setBacklight(255, 255, 255);
    }

    /**
     * Enables or disables the display.
     *
     * @param enabled true to enable, false to disable
     * @throws IOException if there was a communication problem
     */
    public void setDisplayEnabled(boolean enabled) throws IOException {
        if (enabled) {
            displayControl |= DISPLAY_ON;
        } else {
            displayControl &= ~DISPLAY_ON;
        }
        specialCommand((byte) (LCD_DISPLAY_CONTROL | displayControl));
    }

    /**
     * Enables or disables the cursor.
     *
     * @param enabled true to show cursor, false to hide
     * @throws IOException if there was a communication problem
     */
    public void setCursorEnabled(boolean enabled) throws IOException {
        if (enabled) {
            displayControl |= CURSOR_ON;
        } else {
            displayControl &= ~CURSOR_ON;
        }
        specialCommand((byte) (LCD_DISPLAY_CONTROL | displayControl));
    }

    /**
     * Enables or disables cursor blinking.
     *
     * @param enabled true to enable blink, false to disable
     * @throws IOException if there was a communication problem
     */
    public void setBlinkEnabled(boolean enabled) throws IOException {
        if (enabled) {
            displayControl |= BLINK_ON;
        } else {
            displayControl &= ~BLINK_ON;
        }
        specialCommand((byte) (LCD_DISPLAY_CONTROL | displayControl));
    }

    /**
     * @return true if the display is enabled
     */
    public boolean isDisplayEnabled() {
        return (displayControl & DISPLAY_ON) != 0;
    }

    /**
     * @return true if the cursor is enabled
     */
    public boolean isCursorEnabled() {
        return (displayControl & CURSOR_ON) != 0;
    }

    /**
     * @return true if cursor blink is enabled
     */
    public boolean isBlinkEnabled() {
        return (displayControl & BLINK_ON) != 0;
    }

    /**
     * @return true if auto-scroll is enabled
     */
    public boolean isAutoScrollEnabled() {
        return (entryMode & ENTRY_SHIFT_INCREMENT) != 0;
    }

    /**
     * Scrolls the display content to the left by one position.
     *
     * @throws IOException if there was a communication problem
     */
    public void scrollLeft() throws IOException {
        scrollLeft(1);
    }

    /**
     * Scrolls the display content to the left by the specified number of positions.
     *
     * @param count number of positions to scroll
     * @throws IOException if there was a communication problem
     */
    public void scrollLeft(int count) throws IOException {
        for (int i = 0; i < count; i++) {
            specialCommand((byte) (LCD_CURSOR_SHIFT | DISPLAY_MOVE));
        }
    }

    /**
     * Scrolls the display content to the right by one position.
     *
     * @throws IOException if there was a communication problem
     */
    public void scrollRight() throws IOException {
        scrollRight(1);
    }

    /**
     * Scrolls the display content to the right by the specified number of positions.
     *
     * @param count number of positions to scroll
     * @throws IOException if there was a communication problem
     */
    public void scrollRight(int count) throws IOException {
        for (int i = 0; i < count; i++) {
            specialCommand((byte) (LCD_CURSOR_SHIFT | DISPLAY_MOVE | MOVE_RIGHT));
        }
    }

    /**
     * Moves the cursor one position to the left.
     *
     * @throws IOException if there was a communication problem
     */
    public void moveCursorLeft() throws IOException {
        moveCursorLeft(1);
    }

    /**
     * Moves the cursor to the left by the specified number of positions.
     *
     * @param count number of positions to move
     * @throws IOException if there was a communication problem
     */
    public void moveCursorLeft(int count) throws IOException {
        for (int i = 0; i < count; i++) {
            specialCommand((byte) LCD_CURSOR_SHIFT);
        }
    }

    /**
     * Moves the cursor one position to the right.
     *
     * @throws IOException if there was a communication problem
     */
    public void moveCursorRight() throws IOException {
        moveCursorRight(1);
    }

    /**
     * Moves the cursor to the right by the specified number of positions.
     *
     * @param count number of positions to move
     * @throws IOException if there was a communication problem
     */
    public void moveCursorRight(int count) throws IOException {
        for (int i = 0; i < count; i++) {
            specialCommand((byte) (LCD_CURSOR_SHIFT | MOVE_RIGHT));
        }
    }

    /**
     * Sets the text flow direction to left-to-right (default).
     *
     * @throws IOException if there was a communication problem
     */
    public void setLeftToRight() throws IOException {
        entryMode |= ENTRY_LEFT;
        specialCommand((byte) (LCD_ENTRY_MODE_SET | entryMode));
    }

    /**
     * Sets the text flow direction to right-to-left.
     *
     * @throws IOException if there was a communication problem
     */
    public void setRightToLeft() throws IOException {
        entryMode &= ~ENTRY_LEFT;
        specialCommand((byte) (LCD_ENTRY_MODE_SET | entryMode));
    }

    /**
     * Enables auto-scroll: when a character is printed, the display shifts.
     *
     * @param enabled true to enable, false to disable
     * @throws IOException if there was a communication problem
     */
    public void setAutoScrollEnabled(boolean enabled) throws IOException {
        if (enabled) {
            entryMode |= ENTRY_SHIFT_INCREMENT;
        } else {
            entryMode &= ~ENTRY_SHIFT_INCREMENT;
        }
        specialCommand((byte) (LCD_ENTRY_MODE_SET | entryMode));
    }

    /**
     * Sets the display contrast.
     * <p>
     * Lower values produce more visible text (higher contrast), while higher
     * values make text less visible (lower contrast). A value of 50 is
     * recommended. The factory default is 120, which may be too low contrast
     * for many displays.
     * <p>
     * Note: This setting is stored in EEPROM and persists across power cycles.
     *
     * @param contrast contrast level (0-255, lower = more visible text, recommended: 50)
     * @throws IOException if there was a communication problem
     */
    public void setContrast(int contrast) throws IOException {
        contrast = Math.max(0, Math.min(255, contrast));
        writeBytes(new byte[]{SETTING_COMMAND, CONTRAST_COMMAND, (byte) contrast});
        sleep(COMMAND_DELAY_MS);
    }

    /**
     * Sends a raw HD44780 command to the display.
     * <p>
     * This allows access to any HD44780 command not directly exposed by this driver.
     *
     * @param command the HD44780 command byte
     * @throws IOException if there was a communication problem
     */
    public void command(byte command) throws IOException {
        specialCommand(command);
    }

    /**
     * Creates a custom character in CGRAM.
     * <p>
     * After creating the character, use {@link #writeChar(int)} to display it.
     *
     * @param location character location (0-7)
     * @param pattern  8-byte array defining the character pattern (5 bits per row)
     * @throws IOException if there was a communication problem
     */
    public void createChar(int location, byte[] pattern) throws IOException {
        if (location < 0 || location > 7) {
            throw new IllegalArgumentException("Location must be 0-7");
        }
        if (pattern == null || pattern.length != 8) {
            throw new IllegalArgumentException("Pattern must be 8 bytes");
        }

        // SerLCD uses SETTING_COMMAND + (27 + location) + 8 pattern bytes
        byte[] data = new byte[10];
        data[0] = SETTING_COMMAND;
        data[1] = (byte) (CREATE_CHAR_BASE + location);
        System.arraycopy(pattern, 0, data, 2, 8);
        writeBytes(data);
        sleep(COMMAND_DELAY_MS);
    }

    /**
     * Enables or disables the startup splash screen.
     *
     * @param enabled true to show splash on power-up, false to skip
     * @throws IOException if there was a communication problem
     */
    public void setSplashEnabled(boolean enabled) throws IOException {
        settingCommand(enabled ? ENABLE_SPLASH : DISABLE_SPLASH);
    }

    /**
     * Saves the current display content as the splash screen.
     *
     * @throws IOException if there was a communication problem
     */
    public void saveSplash() throws IOException {
        settingCommand(SAVE_SPLASH);
        sleep(SPECIAL_DELAY_MS);
    }

    /**
     * Enables or disables system messages (like address change confirmations).
     *
     * @param enabled true to show system messages, false to suppress
     * @throws IOException if there was a communication problem
     */
    public void setSystemMessagesEnabled(boolean enabled) throws IOException {
        settingCommand(enabled ? ENABLE_SYSTEM_MSG : DISABLE_SYSTEM_MSG);
    }

    /**
     * Changes the I2C address of the display.
     * <p>
     * WARNING: This permanently changes the address stored in the display's EEPROM.
     * Valid addresses are 0x07 to 0x78.
     *
     * @param newAddress the new I2C address
     * @throws IOException if there was a communication problem
     */
    public void setAddress(int newAddress) throws IOException {
        if (newAddress < 0x07 || newAddress > 0x78) {
            throw new IllegalArgumentException("Address must be between 0x07 and 0x78");
        }
        writeBytes(new byte[]{SETTING_COMMAND, ADDRESS_COMMAND, (byte) newAddress});
        sleep(SPECIAL_DELAY_MS);
        LOGGER.info("Display address changed to 0x{}", Integer.toHexString(newAddress));
    }

    /**
     * Stops the display, turning off the backlight and clearing the screen.
     *
     * @throws IOException if there was a communication problem
     */
    public void stop() throws IOException {
        clear();
        setBacklightOff();
        setDisplayEnabled(false);
    }

    /**
     * Sends an HD44780 special command.
     */
    private void specialCommand(byte command) throws IOException {
        writeBytes(new byte[]{SPECIAL_COMMAND, command});
        sleep(COMMAND_DELAY_MS);
    }

    /**
     * Sends a SerLCD setting command.
     */
    private void settingCommand(byte command) throws IOException {
        writeBytes(new byte[]{SETTING_COMMAND, command});
        sleep(COMMAND_DELAY_MS);
    }

    /**
     * Predefined colors for the RGB backlight.
     */
    public enum Color {
        RED(255, 0, 0),
        GREEN(0, 255, 0),
        BLUE(0, 0, 255),
        WHITE(255, 255, 255),
        YELLOW(255, 255, 0),
        CYAN(0, 255, 255),
        MAGENTA(255, 0, 255),
        ORANGE(255, 128, 0),
        PURPLE(128, 0, 255),
        OFF(0, 0, 0);

        private final int red;
        private final int green;
        private final int blue;

        Color(int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public int getRed() {
            return red;
        }

        public int getGreen() {
            return green;
        }

        public int getBlue() {
            return blue;
        }
    }
}
