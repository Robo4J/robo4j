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


    private static final char TURN_ON_OSCILLATOR = 0x21;
    private static final byte ENABLE_DISPLAY_NO_BLINKING = (byte)0x81;
    private static final byte DISABLE_DISPLAY_NO_BLINKING = (byte)0x80;

    private static final char HT16K33_BLINK_CMD = 0x80;
    private static final char HT16K33_BLINK_DISPLAY_ON = 0x01;
    private static final char HT16K33_BLINK_OFF = 0;
    private static final char HT16K33_BLINK_2HZ = 1;
    private static final char HT16K33_BLINK_1HZ = 2;
    private static final char HT16K33_BLINK_HALFHZ = 3;
    private static final char HT16K33_CMD_BRIGHTNESS = 0xE0;
    private static final char LED_OFF = 0;
    private static final char LED_ON = 1;
    private static final byte LED_RED = 1;
    private static final byte LED_YELLOW = 2;
    private static final byte LED_GREEN = 3;

    private static final char FULL_BRIGHTNESS = 15;

    final short[] displayBuffer = new short[8];

    public BarGraphBiLed(int bus, int address) throws Exception {
        super(bus, address);

        begin();
        System.out.println("BEGIN1");

//        for(byte b=0; b<24;b++){
//            setBar(b, LED_RED);
//        }
//        writeDisplay();

        Thread.sleep(2000);
        System.out.println("DONE -> NOWAY");

//        clearDisplay();
//        writeDisplay();
//
        System.out.println("DONE -> CLEANED");
        Thread.sleep(2000);
        System.out.println("DONE -> OFFs");

        i2cDevice.write(DISABLE_DISPLAY_NO_BLINKING);
    }

    private void begin() throws IOException{
        i2cDevice.write((byte)TURN_ON_OSCILLATOR);
        blinkRate(HT16K33_BLINK_OFF);
        setBrightness(FULL_BRIGHTNESS);
    }

    private void blinkRate(char b) throws IOException{
        if(b > 3){
            b = 0;
        }
        i2cDevice.write((byte) (HT16K33_BLINK_CMD | HT16K33_BLINK_DISPLAY_ON | (b << 1)));
    }

    private void setBrightness(char b) throws IOException{
        if(b > 15){
            b = 15;
        }
        i2cDevice.write((byte) (HT16K33_CMD_BRIGHTNESS |  b));
    }

    private void writeDisplay() throws IOException {
        i2cDevice.write((byte)0x00);

        for(byte i=0; i<8;i++){
            int ui = i & 0xFF;
            byte val1 = (byte)(displayBuffer[ui] & 0xFF);
            byte val2 = (byte)(displayBuffer[ui] >> 8);
            i2cDevice.write(val1);
            i2cDevice.write(val2);
        }
    }

    private void clearDisplay() throws IOException {
        for(byte i=0; i<8;i++){
            int ui = i & 0xFF;
            displayBuffer[ui] = (byte) 0;
        }
    }


    private short BV(short bit){
        return (byte) (((byte)1) << bit);
    }



    private void setBar(byte bar, byte color){
        short a, c;
        byte const12 = (byte)(12 & 0xFF) ;
        byte const4 = (byte)(4 & 0xFF);
        byte const8 = (byte)(8 & 0xFF);
        if(bar < const12){
            c = (short) ((bar / const4));
        } else {
            c = (short)(((bar - const12) / const4));
        }

        a = (short)((bar % const4));
        if(bar >= const12){
           a += const4;
        }

//        if (color == 1){
//            displayBuffer[c] =  (displayBuffer[c] | BV(a));
//            short tmp1 = (short) (a + const8);
//            displayBuffer[c] =  (char)(displayBuffer[c] << ~BV(tmp1));
//        } else {
//            short tmp1 = (short) (a + const8);
//            displayBuffer[c] = (char)(displayBuffer[c] << (BV(a) + ~BV(tmp1)));
//        }
    }




}

