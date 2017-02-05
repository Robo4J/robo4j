/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This LcdWrapper.java  is part of robo4j.
 * module: robo4j-hw-lego
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

package com.robo4j.hw.lego.wrapper;

import com.robo4j.hw.lego.ILcd;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class LcdWrapper<LcdType extends GraphicsLCD> implements ILcd {
    private static final String SYSTEM_NAME = "Robo4J.IO";
    private static final String ROBOT_NAME = "Robot : Number42";
    private static final int INCREMENT = 10;
    private int startPosition = 50;


    //TODO : miro -> provide different types of LCDs
    private LcdType lcd;

    @SuppressWarnings("unchecked")
    public LcdWrapper() {
        lcd = (LcdType)BrickFinder.getDefault().getGraphicsLCD();
    }

    //TODO: miro -> make it more generic to setup
    @Override
    public void initiate() {
        lcd.clear();
        lcd.setFont(Font.getLargeFont());
        lcd.drawString(SYSTEM_NAME, 1, 15, GraphicsLCD.LEFT);
        Button.LEDPattern(1);
        lcd.setFont(Font.getSmallFont());
        lcd.drawString(ROBOT_NAME, 2, startPosition, GraphicsLCD.LEFT);
    }

    @Override
    public void printText(int line, String text) {
        if (line > 1) {
            int position = startPosition + (line - 1) * INCREMENT;
            lcd.drawString(text, 2, position, GraphicsLCD.LEFT);
        }
    }

    @Override
    public void printText(String text) {
        startPosition = startPosition + INCREMENT;
        lcd.drawString(text, 2, startPosition, GraphicsLCD.LEFT);
    }
}
