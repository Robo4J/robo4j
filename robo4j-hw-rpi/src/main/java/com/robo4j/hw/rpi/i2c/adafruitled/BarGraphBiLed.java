/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.hw.rpi.i2c.adafruitled;

import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;

import java.io.IOException;

/**
 * Adafruit Bi-Color 24 BarGraph
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class BarGraphBiLed extends AbstractI2CDevice {


    private static final byte TURN_ON_OSCILLATOR = (byte) 0x21;
    private static final byte ENABLE_DISPLAY_NO_BLINKING = (byte)0x81;
    private static final byte HT16K33_BLINK_CMD = (byte)0x80;
    private static final byte HT16K33_BLINK_OFF = (byte) 0;
    private static final byte HT16K33_CMD_BRIGHTNESS = (byte) 0xE0;
    private static final byte HT16K33_BLINK_DISPLAY_ON = (byte)0x01;
    private static final byte DISABLE_DISPLAY_NO_BLINKING = (byte)0x80;
    private static final byte BRIGHTNESS_FULL = (byte) 0xef;
    private static final byte LED_RED = 1;
    private static final byte LED_OFF = 0;

    final byte[] displayBuffer = new byte[8];

    public BarGraphBiLed(int bus, int address) throws Exception {
        super(bus, address);

        begin();
        System.out.println("BEGIN");

        for(byte b=0; b<24;b++){
            setBar(b, LED_RED);
        }
        writeDisplay();

        Thread.sleep(2000);
        System.out.println("DONE -> RED");

        for(byte b=0; b<24;b++){
            setBar(b, LED_OFF);
        }
        writeDisplay();
//
        System.out.println("DONE -> CLEANED");
        Thread.sleep(2000);
        System.out.println("DONE -> OFFs");

        i2cDevice.write(DISABLE_DISPLAY_NO_BLINKING);
    }

    private void begin() throws IOException{
        i2cDevice.write(TURN_ON_OSCILLATOR);
        i2cDevice.write(ENABLE_DISPLAY_NO_BLINKING);
//        blinkRate(HT16K33_BLINK_OFF);
        setBrightness(BRIGHTNESS_FULL);
    }

    private void blinkRate(byte b) throws IOException{
        if(b > (byte)3){
            b = (byte)0;
        }
        i2cDevice.write((byte) (HT16K33_BLINK_CMD | HT16K33_BLINK_DISPLAY_ON | (b << (byte)1)));
    }

    private void setBrightness(byte b) throws IOException{
        if(b > (byte) 15){
            b = (byte)15;
        }
        i2cDevice.write((byte) (HT16K33_CMD_BRIGHTNESS |  b));
    }

    private void writeDisplay() throws Exception {
        i2cDevice.write((byte)0x00);

        for(byte i=0; i<8;i++){
            i2cDevice.write((byte)(displayBuffer[i] & (byte)0xFF));
            i2cDevice.write((byte)(displayBuffer[i] >> (byte)8));
        }
    }


    private byte BV(short bit){
        byte result = (byte) (((byte)1) << bit);
        return result;
    }

    private void setBar(byte bar, byte color){
        short a, c;
        byte const12 = 12;
        byte const4 = 4;
        byte const8 = 8;
        if(bar < const12){
            c = (short) ((bar / const4));
        } else {
            c = (short)(((bar - const12) / const4));
        }

        a = (short)((bar % const4));
        if(bar >= const12){
           a += const4;
        }

        if (color == 1){
            displayBuffer[c] =  (byte)(displayBuffer[c] | BV(a));
            short tmp1 = (short) (a + const8);
            displayBuffer[c] =  (byte)(displayBuffer[c] << ~BV(tmp1));
        } else {
            short tmp1 = (short) (a + const8);
            displayBuffer[c] = (byte)(displayBuffer[c] << (BV(a) + ~BV(tmp1)));
        }
    }




}

