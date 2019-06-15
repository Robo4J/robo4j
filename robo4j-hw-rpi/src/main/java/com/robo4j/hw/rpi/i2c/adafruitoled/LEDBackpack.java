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

import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;

import java.io.IOException;

/**
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
abstract class LEDBackpack extends AbstractI2CDevice {

	static int DEFAULT_I2C_ADDRESS = 0x70;
	static int DEFAULT_BRIGHTNESS = 15;
    private static final int OSCILLATOR_TURN_ON = 0x21;
    private static final int HT16K33_BLINK_CMD = 0x80;
    private static final int HT16K33_BLINK_DISPLAY_ON = 0x01;
    private static final int HT16K33_BLINK_DISPLAY_OFF = 0;
    private static final int HT16K33_CMD_BRIGHTNESS = 0xE0;
    private static final int HT16K33_BLINK_OFF = 0x00;
	static short[] buffer = new short[8]; // uint16_t

    LEDBackpack(int bus, int address) throws IOException {
        super(bus, address);
    }

    void initiate(int brightness) throws IOException {
        i2cDevice.write((byte) (OSCILLATOR_TURN_ON)); // Turn on oscilator
        i2cDevice.write(blinkRate(HT16K33_BLINK_OFF));
        i2cDevice.write(setBrightness(brightness));
    }

    void clearBuffer() throws IOException {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = 0;
        }
    }

    short intToShort(int value) {
        return (short) (value & 0xFFFF);
    }

    void setColorByMatrixToBuffer(short x, short y, BiColor color){
        switch (color){
            case RED:
                // Turn on red LED.
                buffer[y] |= 1 << intToShort(x+8);
                // Turn off green LED.
                buffer[y] &= ~intToShort(1 << x);
                break;
            case YELLOW:
                // Turn on green and red LED.
                buffer[y] |= (1 << intToShort(x+8)) | intToShort(1 << x);
                break;
            case GREEN:
                // Turn on green LED.
                buffer[y] |= intToShort(1 << x);
                // Turn off red LED.
                buffer[y] &= ~intToShort(1 << (x+8));
                break;
            case OFF:
                buffer[y] &= ~intToShort(1 << x) & ~intToShort(1 << intToShort(x+8));
                break;
            default:
                System.out.println("setColorByMatrixToBuffer: " + color);
                break;
        }
    }

    void setColorToBuffer(short a, short c, BiColor color){
        switch (color) {
            case RED:
                // Turn on red LED.
                buffer[c] |= _BV(a);
                // Turn off green LED.
                buffer[c] &= ~_BV(intToShort(a + 8));
                break;
            case YELLOW:
                // Turn on red and green LED.
                buffer[c] |= _BV(a) | _BV(intToShort(a + 8));
                break;
            case GREEN:
                // Turn on green LED.
                buffer[c] |= _BV(intToShort(a + 8));
                // Turn off red LED.
                buffer[c] &= ~_BV(a);
                break;
            case OFF:
                // Turn off red and green LED.
                buffer[c] &= ~_BV(a) & ~_BV(intToShort(a + 8));
                break;
            default:
                System.out.println("setColorToBuffer: " + color);
                break;
        }
    }

    void writeDisplay() throws IOException {
        int address = 0;
        for (int i = 0; i < buffer.length; i++) {
            i2cDevice.write(address++, (byte) (buffer[i] & 0xFF));
            i2cDevice.write(address++, (byte) (buffer[i] >> 8));
        }
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
