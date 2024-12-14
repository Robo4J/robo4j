/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;
import com.robo4j.hw.rpi.utils.I2cBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * AbstractBackpack is the abstraction for all Adafruit Backpack devices
 * https://learn.adafruit.com/adafruit-led-backpack/overview
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public abstract class AbstractBackpack extends AbstractI2CDevice {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBackpack.class);
    public static final int DEFAULT_BRIGHTNESS = 15;
    private static final int DEFAULT_ADDRESS = 0x70;
    private static final int OSCILLATOR_TURN_ON = 0x21;
    private static final int HT16K33_BLINK_CMD = 0x80;
    private static final int HT16K33_BLINK_DISPLAY_ON = 0x01;
    private static final int HT16K33_CMD_BRIGHTNESS = 0xE0;
    private static final int HT16K33_BLINK_OFF = 0x00;
    private final short[] buffer = new short[8]; // uint16_t

    AbstractBackpack() throws IOException {
        this(I2cBus.BUS_1, DEFAULT_ADDRESS, DEFAULT_BRIGHTNESS);
    }

    AbstractBackpack(I2cBus bus, int address, int brightness) throws IOException {
        super(bus, address);
        initiate(brightness);
    }

    public void display() {
        try {
            writeDisplay();
        } catch (IOException e) {
            LOGGER.error("error display:{}", e.getMessage());
        }
    }

    public void clear() {
        try {
            clearBuffer();
        } catch (IOException e) {
            LOGGER.error("error clear:{}", e.getMessage());
        }
    }

    short intToShort(int value) {
        return (short) (value & 0xFFFF);
    }

    /**
     * turn on of the position on the matrix grid
     *
     * @param x     x position on the matrix
     * @param y     y position on the matrix
     * @param color led color
     */
    void setColorByMatrixToBuffer(short x, short y, BiColor color) {
        switch (color) {
            case RED -> {
                // Turn on red LED.
                buffer[y] |= _BV(intToShort(x + 8));
                // Turn off green LED.
                buffer[y] &= (short) ~_BV(x);
            }
            case YELLOW -> {
                // Turn on green and red LED.
                buffer[y] |= (short) (_BV(intToShort(x + 8)) | _BV(x));
            }
            case GREEN -> {
                // Turn on green LED.
                buffer[y] |= _BV(x);
                // Turn off red LED.
                buffer[y] &= (short) ~_BV(intToShort(x + 8));
            }
            case OFF -> buffer[y] &= (short) (~_BV(x) & ~_BV(intToShort(x + 8)));
            default -> LOGGER.warn("setColorByMatrixToBuffer: {}", color);
        }
    }

    /**
     * @param n  position on alphanumeric display
     * @param c  character to be displayed
     * @param dp the dot next to the character
     */
    void setCharacter(int n, int c, boolean dp) {
        short value = (short) c;
        setValue(n, value, dp);
    }

    /**
     * @param n  position
     * @param v  value 16-bits
     * @param dp the dot next to the character
     */
    void setValue(int n, short v, boolean dp) {
        buffer[n] = dp ? (v |= (1 << 14)) : v;
    }

    /**
     * Turn off/on the a led on the bar
     *
     * @param a     position on the bar
     * @param c     position on the bar
     * @param color color on the bar
     */
    void setColorToBarBuffer(short a, short c, BiColor color) {
        switch (color) {
            case RED -> {
                // Turn on red LED.
                buffer[c] |= _BV(a);
                // Turn off green LED.
                buffer[c] &= (short) ~_BV(intToShort(a + 8));
            }
            case YELLOW -> {
                // Turn on red and green LED.
                buffer[c] |= (short) (_BV(a) | _BV(intToShort(a + 8)));
            }
            case GREEN -> {
                // Turn on green LED.
                buffer[c] |= _BV(intToShort(a + 8));
                // Turn off red LED.
                buffer[c] &= (short) ~_BV(a);
            }
            case OFF -> {
                // Turn off red and green LED.
                buffer[c] &= (short) (~_BV(a) & ~_BV(intToShort(a + 8)));
            }
            default -> LOGGER.warn("setColorToBarBuffer: {}", color);
        }
    }

    private void initiate(int brightness) throws IOException {
        writeByte((byte) (OSCILLATOR_TURN_ON)); // Turn on oscilator
        writeByte(blinkRate(HT16K33_BLINK_OFF));
        writeByte(setBrightness(brightness));
    }

    private void writeDisplay() throws IOException {
        int address = 0;
        for (short value : buffer) {
            writeByte(address++, (byte) (value & 0xFF));
            writeByte(address++, (byte) (value >> 8));
        }
    }

    private void clearBuffer() throws IOException {
        Arrays.fill(buffer, (short) 0);
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

    private byte uint8ToByte(int value) {
        return (byte) (value & 0xFF);
    }

}
