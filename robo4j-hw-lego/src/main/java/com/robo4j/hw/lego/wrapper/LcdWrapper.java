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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.hw.lego.wrapper;

import com.robo4j.hw.lego.ILcd;
import com.robo4j.hw.lego.enums.LcdFontEnum;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.lcd.GraphicsLCD;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class LcdWrapper<LcdType extends GraphicsLCD> implements ILcd {
    public static final int SMALL_FONT_INCREMENT = 10;
    public static final String ROBO4J_LOGO = "Robo4j.IO";
    public static final String ROBO4J_ROBOT_NAME = "Number42";
    private int startPosition = 0;

    private LcdType lcd;

    @SuppressWarnings("unchecked")
    public LcdWrapper() {
        lcd = (LcdType)BrickFinder.getDefault().getGraphicsLCD();
    }

    @Override
    public void initRobo4j(String title, String robotName) {
        lcd.clear();
        startPosition = 50;
        lcd.setFont(LcdFontEnum.LARGE.getFont());
        lcd.drawString(title, 1, 15, GraphicsLCD.LEFT);
        Button.LEDPattern(1);
        lcd.setFont(LcdFontEnum.SMALL.getFont());
        lcd.drawString(robotName, 2, startPosition, GraphicsLCD.LEFT);
    }

    @Override
    public void clear() {
        lcd.clear();
    }

    @Override
    public void printText(int line, int increment, String text) {
        if (line > 1) {
            int position = startPosition + (line - 1) * increment;
            lcd.drawString(text, 2, position, GraphicsLCD.LEFT);
        }
    }

    @Override
    public void printText(String text) {
        startPosition = startPosition + SMALL_FONT_INCREMENT;
        lcd.drawString(text, 2, startPosition, GraphicsLCD.LEFT);
    }
}
